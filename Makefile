
JAVA=java
PYTHON=python
R=r

BUILD=build
BOACLIENT=boa-client/lib/boa-client.jar
BOAAPI=boa-client/lib/boa-client-0.1.0.jar
CLASSNAME=ch.usi.inf.sape.boaclient.Main

BOA=unsafe.boa
OUT=$(BOA:%.boa=$(BUILD)/%.out)
CSV=$(OUT:%.out=%.csv)
PDF=$(CSV:%.csv=%-plot-usage-boa-flip.pdf)

.SECONDARY:

all: pdf
pdf: $(PDF)
csv: $(CSV)
out: $(OUT)

%-plot-usage-boa-flip.pdf: %-plot-usage-boa.pdf
#	convert $< -rotate 90 $@
	gs -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit


%-plot-usage-boa.pdf: %.csv unsafe-groups.csv unsafe-methods.csv unsafe-projects.csv unsafe.r
	$(R) --slave --vanilla --file=unsafe.r --args $<

%.csv: %.out unsafe.py
	$(PYTHON) unsafe.py $< $@

$(BUILD)/%.out: %.boa
	$(JAVA) -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) --dataset "2013 September" --input $< --output $@

clean:
	rm -f $(OUT) $(CSV) $(PDF)
