using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using WeaPanel_SDK.exceptions;
using WeaPanel_SDK.sdk.api;
using WeaPanel_SDK.sdk.api.requests;

namespace WeaPanel_SDK.sdk
{
    public struct WeatherData
    {
        public readonly double temperature;
        public readonly int humidity;
        public readonly int pressure_hPa;
        public readonly int pressure_Pa;
        public readonly int MQ135;
        public readonly int MQ7;
        public readonly int MQ2;

        internal WeatherData(double temperature, int humidity, int pressure_hPa, int pressure_Pa,
            int MQ135, int MQ7, int MQ2)
        {
            this.temperature = temperature;
            this.humidity = humidity;
            this.pressure_hPa = pressure_hPa;
            this.pressure_Pa = pressure_Pa;
            this.MQ135 = MQ135;
            this.MQ7 = MQ7;
            this.MQ2 = MQ2;
        }
    }
    public class WeaStation
    {
        private WeaSDK.WeaSDK sdk;
        public WeaStation(WeaSDK.WeaSDK sdk)
        {
            this.sdk = sdk;
        }

        public WeatherData GetWeatherData()
        {
            WeatherData weatherData;
            RequestHandler<WeatherDataRequest.ResponseBody> request =
                new RequestHandler<WeatherDataRequest.ResponseBody>
                (new WeatherDataRequest(sdk.GetAuthentication().token,
                sdk.GetAuthentication().uuid));
            Response<WeatherDataRequest.ResponseBody> response = request.SendRequest();
           
            if(response.code == 200)
            {
                weatherData = new WeatherData(response.responseBody.temperature,
                                              response.responseBody.humidity,
                                              response.responseBody.pressure_hPa,
                                              response.responseBody.pressure_Pa,
                                              response.responseBody.MQ135,
                                              response.responseBody.MQ7,
                                              response.responseBody.MQ2);
            }
            else if (response.code == 503)
            {
                throw new MaintenanceException();
            }
            else
            {
                throw new NoConnectionException();
            }
            return weatherData;
        }

    }
}
