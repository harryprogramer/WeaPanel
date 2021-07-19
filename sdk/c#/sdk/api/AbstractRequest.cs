using RestSharp;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;

namespace WeaPanel_SDK.sdk.api
{
    public abstract class AbstractRequest
    {
        private readonly int packetId;
        private readonly Method method;
        
        protected AbstractRequest(int id, Method method)
        {
            packetId = id;
            this.method = method;
        }

      
        public abstract void OnStartRequest();
        public abstract void OnEndRequest();

        public int id { get; set; }
        public string token { get; set; }
        public string account_uuid { get; set; }

        protected int GetID()
        {
            return id;
        }

        internal Method getMethod()
        {
            return method;
        }
    }
}
