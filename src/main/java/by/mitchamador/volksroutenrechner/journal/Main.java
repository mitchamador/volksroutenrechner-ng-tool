package by.mitchamador.volksroutenrechner.journal;

import by.mitchamador.volksroutenrechner.journal.object.Journal;
import by.mitchamador.volksroutenrechner.journal.object.JournalEntry;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("import").argName("journal file name").hasArgs().required().desc("Import journal").build());

        try {
            CommandLine commandLine = new DefaultParser().parse(options, args);

            if (commandLine.hasOption("import")) {
                Main main = new Main(commandLine.getOptionValues("import"));
                main.run();
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

    }

    private final String[] filenames;

    public Main(String[] filenames) {
        this.filenames = filenames;
    }

    private void run() throws IOException {
        Journal mainJournal = new Journal();
        for (String filename : filenames) {
            for (File f : FileUtils.listFiles(new File(FilenameUtils.getPath(filename)), new WildcardFileFilter(FilenameUtils.getName(filename)), null)) {
                if (!f.isDirectory()) {
                    Journal journal = Journal.create(f.getCanonicalPath());
                    for (JournalEntry item : journal.getEntries()) {
                        mainJournal.addEntryData(item);
                    }
                }
            }
        }
        mainJournal.sortEntries();
        System.out.print(mainJournal.getPrintableString(true));

        byte[] array = mainJournal.toByteArray(768, true);

        //System.out.println(new Journal(array).getPrintableString(true));
        FileUtils.writeByteArrayToFile(new File("out.bin"), array);
        System.out.print(convertByteArrayToCode(array));
    }

    private String convertByteArrayToCode(byte[] array) {
        StringBuilder s = new StringBuilder(array.length);
        s.append("const char data[] = {");
        int c = 0;
        while (c < array.length) {
            s.append((c % 16) == 0 ? ",\n  " : ",").append(String.format("0x%02x", array[c++]));
        }
        s.append("\n}\n");
        return s.toString();
    }

}
