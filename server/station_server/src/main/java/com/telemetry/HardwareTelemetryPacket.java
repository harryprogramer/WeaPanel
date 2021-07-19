package com.telemetry;

public interface HardwareTelemetryPacket {
    class Builder implements HardwareTelemetryPacket {
        final boolean dht, soundMeter, sdCard, firstBlock, secondBlock, MQ135, MQ7, MQ2, barometer, gy49;
        final int freeMemory, firstBlockV, secondBlockV;

        public Builder(boolean dht, boolean soundMeter,
                       boolean sdCard, boolean firstBlock,
                       boolean secondBlock, boolean MQ135,
                       boolean MQ7, boolean MQ2, boolean barometer,
                       boolean gy49,
                       int memory, int firstBlockVoltage, int secondBlockVoltage){
             this.dht = dht;
             this.soundMeter = soundMeter;
             this.sdCard = sdCard;
             this.firstBlock = firstBlock;
             this.secondBlock = secondBlock;
             this.MQ135 = MQ135;
             this.MQ7 = MQ7;
             this.MQ2 = MQ2;
             this.barometer = barometer;
             this.freeMemory = memory;
             this.firstBlockV = firstBlockVoltage;
             this.secondBlockV = secondBlockVoltage;
             this.gy49 = gy49;
        }

        @Override
        public boolean dht_Status() {
            return dht;
        }

        @Override
        public boolean soundMeter_Status() {
            return soundMeter;
        }

        @Override
        public boolean sdCard_Status() {
            return sdCard;
        }

        @Override
        public boolean firstBlock_Status() {
            return firstBlock;
        }

        @Override
        public boolean secondBlock_Status() {
            return secondBlock;
        }

        @Override
        public boolean MQ135_Status() {
            return MQ135;
        }

        @Override
        public boolean MQ7_Status() {
            return MQ7;
        }

        @Override
        public boolean MQ2_Status() {
            return MQ2;
        }

        @Override
        public boolean barometerStatus() {
            return barometer;
        }

        @Override
        public boolean GY49_Sensor_Status() {
            return gy49;
        }

        @Override
        public int freeARMMemory() {
            return freeMemory;
        }

        @Override
        public int firstBlockVoltage() {
            return firstBlockV;
        }

        @Override
        public int secondBlockVoltage() {
            return secondBlockV;
        }
    }

    boolean dht_Status();

    boolean soundMeter_Status();

    boolean sdCard_Status();

    boolean firstBlock_Status();

    boolean secondBlock_Status();

    boolean MQ135_Status();

    boolean MQ7_Status();

    boolean MQ2_Status();

    boolean barometerStatus();

    boolean GY49_Sensor_Status();

    int freeARMMemory();

    int firstBlockVoltage();

    int secondBlockVoltage();
}
