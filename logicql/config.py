

from lbconfig.api import *
lbconfig_package('application', version='0.1',
		                 default_targets=['lb-libraries'])
depends_on(logicblox_dep)
lb_library(name='application', srcdir='.')
check_lb_workspace(name='application', libraries=['application'])

