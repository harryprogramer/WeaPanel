using System;
using System.Runtime.InteropServices;
using WeaPanel_SDK.exceptions;
using WeaSDK.sdk.api;

namespace WeaSDK
{
    public struct Authentication
    {
        public readonly string uuid;
        public readonly string token;

        public Authentication(string uuid, string token)
        {
            this.uuid = uuid;
            this.token = token;
        }
       
    }
    public class Config
    {
        public static string APIURL = "http://apiwpanel.lagpixel.pl/api/v2";
    }

    public class WeaSDK
    {
        private readonly Authentication auth;

        /// <summary>Autoryzacja API</summary>
        /// <exception cref="UnknownAuthException">Jesli dane uwierzytelniania sa wadliwe, ten watek zostanie zrzutowany</exception>
        public WeaSDK(Authentication auth)
        {
            this.auth = auth;
            AuthResult result = (AuthResult)ApiUtils.testAuth(auth.token, auth.uuid);
            if (!result.status) 
            {
                throw new InvalidAuthException();
            }
        }

        internal Authentication GetAuthentication()
        {
            return auth;
        }

        [DllImport("kernel32.dll", SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        private static extern bool AllocConsole();

        public static void DebugConsole()
        {
            AllocConsole();
        }
       
    }
}
