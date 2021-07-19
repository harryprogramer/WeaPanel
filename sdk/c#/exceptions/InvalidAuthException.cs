using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WeaPanel_SDK.exceptions
{
    class InvalidAuthException : Exception
    {
        public InvalidAuthException() : base("UUID konta albo token API jest niepoprawny")
        {
        }
    }
}
