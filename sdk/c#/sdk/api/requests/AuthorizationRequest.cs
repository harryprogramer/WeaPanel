using System;
using System.Collections.Generic;
using System.Text;
using WeaPanel_SDK.sdk.api;

namespace WeaSDK.sdk.api.requests
{
    internal class AuthorizationRequest : AbstractRequest
    {
        public class ResponseBody
        {
            public String message { get; set; }
        }
        public AuthorizationRequest(string token, string uuid) : base(2, RestSharp.Method.GET)
        {
            base.token = token;
            base.account_uuid = uuid;
            base.id = 2;
        }

        public override void OnEndRequest()
        {
            throw new NotImplementedException();
        }

        public override void OnStartRequest()
        {
            throw new NotImplementedException();
        }

    }
}
