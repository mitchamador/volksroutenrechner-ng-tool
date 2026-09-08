package by.mitchamador.volksroutenrechner.journal;

public class Config {

    private String[] input;

    private String output;

    private int outputSize;

    public String[] getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public static class Builder {

        private String[] input;

        private String output;

        private int outputSize;

        public Builder setInput(String[] input) {
            this.input = input;
            return this;
        }

        public Builder setOutput(String output) {
            this.output = output;
            return this;
        }

        public Builder setOutputSize(String outputSize) {
            if (outputSize != null) {
                try {
                    this.outputSize = Integer.parseInt(outputSize);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return this;
        }

        public Config build() {
            Config config = new Config();
            config.input = input;
            config.output = output;
            config.outputSize = outputSize;
            return config;
        }
    }
}
