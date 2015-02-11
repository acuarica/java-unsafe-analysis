
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
PDF=$(CSV:%.csv=%-plot-usage.pdf)

.SECONDARY:

all: pdf
pdf: $(PDF)
csv: $(CSV)
out: $(OUT)

%-plot-usage.pdf: %.csv unsafe.r
	$(R) --slave --vanilla --file=unsafe.r --args $<

%.csv: %.out unsafe.py
	$(PYTHON) unsafe.py $< $@

$(BUILD)/%.out: %.boa
	$(JAVA) -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) --dataset "2013 September" --input $< --output $@

clean:
	rm -f $(OUT) $(CSV) $(PDF)
