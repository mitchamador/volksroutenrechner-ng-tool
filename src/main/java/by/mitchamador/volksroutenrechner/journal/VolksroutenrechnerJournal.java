package by.mitchamador.volksroutenrechner.journal;

import by.mitchamador.volksroutenrechner.journal.object.Journal;
import by.mitchamador.volksroutenrechner.journal.object.JournalEntry;
import by.mitchamador.volksroutenrechner.journal.object.JournalItem;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.IOException;

public class VolksroutenrechnerJournal {

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("import").argName("journal file name").hasArg().required().desc("Import journal").build());
        options.addOption(Option.builder("o").longOpt("output").argName("journal output name").optionalArg(true).hasArg().desc("Output journal").build());
        options.addOption(Option.builder("os").longOpt("output-size").argName("journal output size name").hasArg().desc("Output journal's size (default 2048)").build());

        try {
            CommandLine commandLine = new DefaultParser().parse(options, args);

            Config config = new Config.Builder()
                    .setInput(commandLine.getOptionValues("import"))
                    .setOutput(commandLine.getOptionValue("output"))
                    .setOutputSize(commandLine.getOptionValue("output-size"))
                    .build();

            if (config.getInput() != null) {
                VolksroutenrechnerJournal volksroutenrechnerJournal = new VolksroutenrechnerJournal(config);
                volksroutenrechnerJournal.run();
            } else {
                System.out.println("No input files");
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

    }

    private final Config config;

    public VolksroutenrechnerJournal(Config config) {
        this.config = config;
    }

    private void run() throws IOException {
        Journal mainJournal = new Journal();
        for (String filename : config.getInput()) {
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

        if (config.getOutputSize() > 0) {
            createJournalEeprom(mainJournal.toByteArray(config.getOutputSize(), JournalItem.ITEM_V2), config.getOutput());
        }

    }

    private void createJournalEeprom(byte[] array, String filename) throws IOException {
        if (array == null) return;

        if (filename != null) {
            FileUtils.writeByteArrayToFile(new File(filename), array);
        } else {
            System.out.print(convertByteArrayToCode(array));
        }

    }

    private String convertByteArrayToCode(byte[] array) {
        StringBuilder s = new StringBuilder(array.length);
        s.append("const char data[] = {");
        int c = 0;
        while (c < array.length) {
            s.append(c > 0 ? "," : "").append((c % 16) == 0 ? "\n  " : "").append(String.format("0x%02x", array[c++]));
        }
        s.append("\n}\n");
        return s.toString();
    }

}
