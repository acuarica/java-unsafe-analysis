logicblox = /Users/luigi/Downloads/logicblox-x86_64-darwin-4.2.0-9bdb05fbcc59
version = 0.1
package_basename = application
package_name = application-$(version)
file = config.py
prefix = out
help = 0
extension = None
lb = /Users/luigi/Downloads/logicblox-x86_64-darwin-4.2.0-9bdb05fbcc59/bin/lb
V = 0
Q0 = @
Q1 = 
Q = $(Q$(V))
topdir = $(shell pwd)
build = $(topdir)/build
protoc = protoc
proto2datalog = $(logicblox)/bin/proto2datalog
testcase = 
all : \
	lb-libraries

$(build)/check_workspace_ws-application.success : \
	$(build)/sepcomp/application/LB_SUMMARY.lbp

	@echo Building workspace application >&2
	$(Q)rm -f $(build)/check_workspace_ws-application.success&&$(lb) create application --overwrite&&$(lb) addproject application $(build)/sepcomp/application --libpath $(build)/sepcomp&&mkdir -p $(build) && touch $(build)/check_workspace_ws-application.success

$(build)/sepcomp/application : \
	$(build)/sepcomp/application/LB_SUMMARY.lbp

$(build)/sepcomp/application/LB_SUMMARY.lbp : \
	./application.project \
	./core/maven.logic \
	./data/init.logic

	@echo Compiling logicblox project application >&2
	$(Q)mkdir -p $(build)/sepcomp/application
	$(Q)LOGICBLOX_HOME=$(logicblox)  sh -c '$(logicblox)/bin/lb compile project --libpath $(build)/sepcomp --out-dir $(build)/sepcomp/application ./application.project' || ( rm -f $(build)/sepcomp/application/LB_SUMMARY.lbp; exit 1)
	$(Q)touch $(build)/sepcomp/application/LB_SUMMARY.lbp

% : \
	%.sh

.PHONY: application
application : \
	$(build)/sepcomp/application/LB_SUMMARY.lbp

.PHONY: check
check : \
	all

check-argument-logicblox :

	$(Q)test -e $(logicblox)

check-lb-libraries :

check-lb-workspaces : \
	check-ws-application

.PHONY: check-ws-application
check-ws-application : \
	$(build)/check_workspace_ws-application.success

clean : \
	clean-workspaces

	rm -rf $(build)/sepcomp/application

clean-workspaces :

	@echo Cleaning up >&2
	$(Q)-if [ -e $(build)/check_workspace_ws-application.success ]; then lb delete application;fi
	$(Q)rm -f $(build)/check_workspace_ws-application.success

dist : \
	dist_files

	@echo Creating dist tarball >&2
	$(Q)tar zcvf $(package_name).tar.gz $(package_name)
	$(Q)rm -rf $(package_name)

dist_dir : \
	config.py \
	./application.project \
	./core/maven.logic \
	./data/init.logic

	@echo Creating dist dir >&2
	$(Q)rm -rf $(package_name)
	$(Q)mkdir -p $(package_name)

dist_files : \
	dist_dir

	@echo Copying files to dist dir >&2
	$(Q)mkdir -p $(package_name)/`dirname ./application.project`
	$(Q)cp -f ./application.project $(package_name)/`dirname ./application.project`
	$(Q)mkdir -p $(package_name)/`dirname ./core/maven.logic`
	$(Q)cp -f ./core/maven.logic $(package_name)/`dirname ./core/maven.logic`
	$(Q)mkdir -p $(package_name)/`dirname ./data/init.logic`
	$(Q)cp -f ./data/init.logic $(package_name)/`dirname ./data/init.logic`
	$(Q)mkdir -p $(package_name)/`dirname config.py`
	$(Q)cp -f config.py $(package_name)/`dirname config.py`

distcheck : \
	dist

	@echo Testing dist tarball >&2
	$(Q)rm -rf $(build)/distcheck
	$(Q)rm -rf $(build)/$(prefix)
	$(Q)mkdir -p $(build)/distcheck
	$(Q)cd $(build)/distcheck && tar zxf ../../$(package_name).tar.gz
	$(Q)cd $(build)/distcheck/$(package_name) &&lb config --prefix=$(build)/$(prefix)
	$(Q)cd $(build)/distcheck/$(package_name) && $(MAKE)
	$(Q)cd $(build)/distcheck/$(package_name) && $(MAKE) install

.PHONY: install
install : \
	all \
	$(build)/sepcomp/application

	@echo Installing files >&2
	$(Q)mkdir -p $(prefix)/share/$(package_basename)/application && cp -fR $(build)/sepcomp/application/* $(prefix)/share/$(package_basename)/application

lb-libraries : \
	$(build)/sepcomp/application/LB_SUMMARY.lbp

