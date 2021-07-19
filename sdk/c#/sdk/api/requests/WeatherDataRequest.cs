using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WeaPanel_SDK.sdk.api.requests
{
    internal class WeatherDataRequest : AbstractRequest
    {
        internal class ResponseBody
        {
            public double temperature { get; set; }
            public int humidity { get; set; }
            public int pressure_hPa { get; set; }
            public int pressure_Pa { get; set; }
            public int MQ135 { get; set; }
            public int MQ7 { get; set; }
            public int MQ2 { get; set; }

        }

        public WeatherDataRequest(string token, string uuid) : base(1, RestSharp.Method.GET) {

            base.id = 1;
            base.token = token;
            base.account_uuid = uuid;
        }
        public override void OnEndRequest()
        {
            
        }

        public override void OnStartRequest()
        {
           
        }
    }
}
