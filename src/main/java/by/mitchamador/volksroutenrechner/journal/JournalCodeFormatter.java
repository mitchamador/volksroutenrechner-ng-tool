package by.mitchamador.volksroutenrechner.journal;

/**
 * Форматирование байтового массива в виде C-исходника (const char data[] = {...}).
 * Логика идентична приватному методу convertByteArrayToCode в VolksroutenrechnerJournal,
 * вынесена сюда, чтобы её мог использовать и веб-экспорт.
 */
public class JournalCodeFormatter {

    public static String toCSource(byte[] array) {
        StringBuilder s = new StringBuilder(array.length * 5);
        s.append("const char data[] = {");
        int c = 0;
        while (c < array.length) {
            s.append(c > 0 ? "," : "").append((c % 16) == 0 ? "\n  " : "").append(String.format("0x%02x", array[c++]));
        }
        s.append("\n}\n");
        return s.toString();
    }
}
