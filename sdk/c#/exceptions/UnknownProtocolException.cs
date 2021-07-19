using System;
using System.Collections.Generic;
using System.Text;

namespace WeaSDK.exceptions
{
    class UnknownProtocolException : Exception
    {
        public UnknownProtocolException(string message) : base(message)
        {

        }
    }
}
