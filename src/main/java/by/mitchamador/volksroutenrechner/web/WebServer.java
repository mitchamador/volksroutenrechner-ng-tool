package by.mitchamador.volksroutenrechner.web;

import by.mitchamador.volksroutenrechner.db.JournalExporter;
import by.mitchamador.volksroutenrechner.db.JournalImporter;
import by.mitchamador.volksroutenrechner.db.JournalRepository;
import by.mitchamador.volksroutenrechner.db.record.AccelRecord;
import by.mitchamador.volksroutenrechner.db.record.TripRecord;
import by.mitchamador.volksroutenrechner.journal.JournalCodeFormatter;
import by.mitchamador.volksroutenrechner.journal.object.Journal;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebServer {

    private final JournalRepository repository;

    public WebServer(JournalRepository repository) {
        this.repository = repository;
    }

    public Javalin start(int port) {
        Javalin app = Javalin.create(config -> config.staticFiles.add("/public", Location.CLASSPATH));

        app.get("/api/trips/{type}", this::getTrips);
        app.delete("/api/trips/{type}/{id}", this::deleteTrip);
        app.put("/api/trips/{type}/{id}", this::updateTrip);

        app.get("/api/accel", this::getAccels);
        app.delete("/api/accel/{id}", this::deleteAccel);
        app.put("/api/accel/{id}", this::updateAccel);

        app.post("/api/import", this::importFile);
        app.get("/api/export", this::exportJournal);

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(errorBody(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        });

        return app.start(port);
    }

    private void getTrips(Context ctx) throws Exception {
        char type = parseTripType(ctx.pathParam("type"));
        Long from = parseLongParam(ctx.queryParam("from"));
        Long to = parseLongParam(ctx.queryParam("to"));
        int pageSize = parsePageSize(ctx.queryParam("pageSize"));
        int page = parsePage(ctx.queryParam("page"));

        List<TripRecord> records = repository.findTrips(type, from, to, pageSize, (page - 1) * pageSize);
        int total = repository.countTrips(type, from, to);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("since", repository.getSinceTime(type));
        body.put("records", records);
        body.put("total", total);
        body.put("page", page);
        body.put("pageSize", pageSize);
        ctx.json(body);
    }

    private void deleteTrip(Context ctx) throws Exception {
        repository.deleteTrip(Long.parseLong(ctx.pathParam("id")));
        ctx.status(204);
    }

    private void updateTrip(Context ctx) throws Exception {
        TripRecord record = ctx.bodyAsClass(TripRecord.class);
        record.setId(Long.parseLong(ctx.pathParam("id")));
        repository.updateTrip(record);
        ctx.status(204);
    }

    private void getAccels(Context ctx) throws Exception {
        Long from = parseLongParam(ctx.queryParam("from"));
        Long to = parseLongParam(ctx.queryParam("to"));
        int pageSize = parsePageSize(ctx.queryParam("pageSize"));
        int page = parsePage(ctx.queryParam("page"));

        List<AccelRecord> records = repository.findAccels(from, to, pageSize, (page - 1) * pageSize);
        int total = repository.countAccels(from, to);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("records", records);
        body.put("total", total);
        body.put("page", page);
        body.put("pageSize", pageSize);
        ctx.json(body);
    }

    private void deleteAccel(Context ctx) throws Exception {
        repository.deleteAccel(Long.parseLong(ctx.pathParam("id")));
        ctx.status(204);
    }

    private void updateAccel(Context ctx) throws Exception {
        AccelRecord record = ctx.bodyAsClass(AccelRecord.class);
        record.setId(Long.parseLong(ctx.pathParam("id")));
        repository.updateAccel(record);
        ctx.status(204);
    }

    private void importFile(Context ctx) throws Exception {
        UploadedFile file = ctx.uploadedFile("file");
        if (file == null) {
            ctx.status(400).json(errorBody("не передан файл (поле 'file')"));
            return;
        }
        byte[] data;
        try (InputStream in = file.content()) {
            data = readAll(in);
        }
        Journal journal = new Journal(data);
        JournalImporter.ImportResult result = new JournalImporter(repository).importJournal(journal);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("imported", result.getImported());
        body.put("skipped", result.getSkipped());
        ctx.json(body);
    }

    private void exportJournal(Context ctx) throws Exception {
        String format = ctx.queryParam("format");
        if (format == null) {
            format = "bin";
        }
        String sizeParam = ctx.queryParam("size");
        int size = sizeParam != null ? Integer.parseInt(sizeParam) : 2048;

        Journal journal = new JournalExporter(repository).buildJournal();
        byte[] data = journal.toByteArray(size);

        if ("c".equalsIgnoreCase(format)) {
            String code = JournalCodeFormatter.toCSource(data);
            ctx.contentType("text/plain; charset=UTF-8");
            ctx.header("Content-Disposition", "attachment; filename=\"journal_" + size + ".c\"");
            ctx.result(code);
        } else {
            ctx.contentType("application/octet-stream");
            ctx.header("Content-Disposition", "attachment; filename=\"journal_" + size + ".bin\"");
            ctx.result(data);
        }
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }

    private char parseTripType(String raw) {
        if (raw == null || raw.length() != 1) {
            throw new IllegalArgumentException("некорректный тип трипа: " + raw);
        }
        char type = Character.toUpperCase(raw.charAt(0));
        if (type != 'C' && type != 'A' && type != 'B') {
            throw new IllegalArgumentException("некорректный тип трипа: " + raw);
        }
        return type;
    }

    private Long parseLongParam(String value) {
        return value == null || value.isEmpty() ? null : Long.parseLong(value);
    }

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 500;

    private int parsePageSize(String raw) {
        int size = (raw == null || raw.isEmpty()) ? DEFAULT_PAGE_SIZE : Integer.parseInt(raw);
        if (size < 1) {
            size = 1;
        } else if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE; // защита от чрезмерно тяжёлых запросов
        }
        return size;
    }

    private int parsePage(String raw) {
        int page = (raw == null || raw.isEmpty()) ? 1 : Integer.parseInt(raw);
        return Math.max(page, 1);
    }

    private byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
