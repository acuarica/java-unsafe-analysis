
JAVA=java
PYTHON=python
R=r

BUILD=build
BOACLIENT=../boa-client/lib/boa-client.jar
BOAAPI=../boa-client/lib/boa-client-0.1.0.jar
CLASSNAME=ch.usi.inf.sape.boaclient.Main
AUTH=--user luismastrangelo --pass elotrodia

BOAS=$(wildcard *.boa)
OUTS=$(BOAS:%.boa=$(BUILD)/%.out)
CSVS=$(OUTS:%.out=%.csv)
PDFS=$(CSVS:%.csv=%-plot-java-over-total.pdf) $(CSVS:%.csv=%-plot-unsafe-over-java.pdf) $(CSVS:%.csv=%-plot-usage.pdf)

.SECONDARY:

all: $(PDFS)

%-plot-java-over-total.pdf %-plot-unsafe-over-java.pdf %-plot-usage.pdf: %.csv unsafe.r
	$(R) --slave --vanilla --file=unsafe.r --args $<

%.csv: %.out unsafe.py
	$(PYTHON) unsafe.py $< $@

$(BUILD)/%.out: %.boa
	$(JAVA) -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) $(AUTH) --dataset "2013 September" --input $< --output $@

clean:
	rm -f $(OUTS) $(CSVS) $(PDFS)
