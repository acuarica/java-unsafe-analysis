
R=r
BUILD=build

CSCSV=$(BUILD)/unsafe-maven-cs.csv
CSPDFPRE=$(BUILD)/unsafe-maven-cs-all-vertical-pre.pdf
CSPDF=$(BUILD)/unsafe-maven-cs-all-vertical.pdf

cspdf: $(CSPDF)
cspdfpre: $(CSPDFPRE)
cscsv: $(CSCSV)

$(CSPDF): $(CSPDFPRE)
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit

$(CSPDFPRE): $(CSCSV) unsafe.r | $(BUILD)
	$(R) --slave --vanilla --file=unsafe.r

$(CSCSV): unsafe-maven.csv unsafe-def-groups.csv unsafe-def-methods.csv cs.r | $(BUILD)
	$(R) --slave --vanilla --file=cs.r

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
