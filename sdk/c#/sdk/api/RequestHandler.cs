using Newtonsoft.Json;
using RestSharp;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace WeaPanel_SDK.sdk.api
{
    public struct Response<R> {
        public readonly int code;
        public readonly int executeTime_Millis;
        public readonly R responseBody;

        internal Response(int code, int executeTime_Millis, R responseObj)
        {
            this.code = code;
            this.executeTime_Millis = executeTime_Millis;
            this.responseBody = responseObj;
        } 

    }

    public class RequestHandler<R>
    {
        private readonly RestClient client = new RestClient(WeaSDK.Config.APIURL);
        private readonly AbstractRequest requestPatern;
        internal RequestHandler(AbstractRequest request)
        {
            this.requestPatern = request;
        }
        internal Response<R> SendRequest()
        {
            Console.WriteLine(WeaSDK.Config.APIURL);
            Console.WriteLine(requestPatern.getMethod());
            Console.WriteLine(requestPatern.ToString());
            RestRequest request = new RestRequest(requestPatern.getMethod());
            request.AddParameter("token", requestPatern.token);
            request.AddParameter("uuid", requestPatern.account_uuid);
            Console.WriteLine("id: " + requestPatern.id);
            request.AddParameter("id", requestPatern.id);
            request.AddHeader("Content-Type", "application/json");
            var response = client.Execute(request);
            Console.WriteLine(response.Content);
            Response<R> responseStruct = new Response<R>((int) response.StatusCode, 0, JsonConvert.DeserializeObject<R>(response.Content));
            return responseStruct; 

        }
    }
}
