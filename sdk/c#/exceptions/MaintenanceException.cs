using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace WeaPanel_SDK.exceptions
{
    class MaintenanceException : Exception
    {
        public MaintenanceException() : base("Z powodu awarii lub przestoju API nie jest aktualnie dostępne, spróbuj ponownie poźniej lub skontaktuj się z manym") { }
    }
}
