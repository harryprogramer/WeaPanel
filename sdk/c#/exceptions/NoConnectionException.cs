using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WeaPanel_SDK.exceptions
{
    class NoConnectionException : Exception
    {
        public NoConnectionException() : base("Brak polaczenia z serwerami WPanel") { }
    }
}
