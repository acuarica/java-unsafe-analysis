
BOACLIENT=../boa-client/lib/boa-client.jar
BOAAPI=../boa-client/lib/boa-client-0.1.0.jar
CLASSNAME=ch.usi.inf.sape.boaclient.Main
AUTH=--user luismastrangelo --pass elotrodia

BOAS=$(wildcard *.boa)
OUTS=$(BOAS:%.boa=%.out)
CSVS=$(OUTS:%.out=%.csv)
PDFS=$(CSVS:%.csv=%-java-over-total.pdf) $(CSVS:%.csv=%-unsafe-over-java.pdf) $(CSVS:%.csv=%-usage.pdf)

all: $(PDFS)

%-java-over-total.pdf %-unsafe-over-java.pdf %-usage.pdf: %.csv unsafe.r
	r --slave --vanilla --file=unsafe.r --args $<

%.csv: %.out unsafe.py
	python unsafe.py $< $@

%.out: %.boa
	java -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) $(AUTH) --dataset "2013 September" --input $< --output $@
