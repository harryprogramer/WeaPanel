package com.telemetry;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public interface TelemetryPacket {
    class Builder implements TelemetryPacket {
        LocalDateTime date = LocalDateTime.now();
        final float temperature;
        final float humidity;
        final float light_lux;
        final int sound;
        final int mq135;
        final int mq7;
        final int mq3;
        final int pressure_hPa;
        final int pressure_pa;

        public Builder(float temperature, float humidity,
                       int sound, int mq135, int mq7,
                       int mq3, int pressure_hPa, int pressure_pa, float light_lux){
            this.temperature = temperature;
            this.humidity = humidity;
            this.sound = sound;
            this.mq135 = mq135;
            this.mq7 = mq7;
            this.mq3 = mq3;
            this.pressure_hPa = pressure_hPa;
            this.pressure_pa = pressure_pa;
            this.light_lux = light_lux;
        }

        @Override
        public float getTemperature() {
            return temperature;
        }

        @Override
        public float getHumidity() {
            return humidity;
        }

        @Override
        public int getSoundLevel() {
            return sound;
        }

        @Override
        public int getMQ135_ppm() {
            return mq135;
        }

        @Override
        public int getMQ7_ppm() {
            return mq7;
        }

        @Override
        public int getMQ3_ppm() {
            return mq3;
        }

        @Override
        public int getPressure_hPa() {
            return pressure_hPa;
        }

        @Override
        public int getPressure_pa() {
            return pressure_pa;
        }

        @Override
        public float getLight_Lux() {
            return light_lux;
        }

        @Override
        public LocalDateTime getTime() {
            return date;
        }
    }

    float getTemperature();

    float getHumidity();

    int getSoundLevel();

    int getMQ135_ppm();

    int getMQ7_ppm();

    int getMQ3_ppm();

    int getPressure_hPa();

    int getPressure_pa();

    float getLight_Lux();

    LocalDateTime getTime();
}
