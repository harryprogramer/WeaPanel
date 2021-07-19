using System;
using System.Collections.Generic;
using System.Text;
using WeaPanel_SDK.exceptions;
using WeaPanel_SDK.sdk.api;
using WeaSDK.exceptions;
using WeaSDK.sdk.api.requests;

namespace WeaSDK.sdk.api
{
    public struct AuthResult
    {
        public readonly string message;
        public readonly bool status;

        internal AuthResult(string message, bool status)
        {
            this.message = message;
            this.status = status;
        }
    }
    internal class ApiUtils
    {
        internal static AuthResult? testAuth(string token, string uuid)
        {
            AuthResult result;
            RequestHandler<AuthorizationRequest.ResponseBody> request
                = new RequestHandler<AuthorizationRequest.ResponseBody>
                (new AuthorizationRequest(token, uuid));
            Response<AuthorizationRequest.ResponseBody> response = request.SendRequest();
            if (response.code == 200)
            {
                result = new AuthResult(response.responseBody.message, true);
            }
            else if (response.code == 403)
            {
                result = new AuthResult(response.responseBody.message, false);
            }
            else if (response.code == 0)
            {
                throw new NoConnectionException(); 
            } 
            else
            {
                throw new UnknownProtocolException("Unknown http status when trying to test auth");
            }

            return result;
        }
    }
}
