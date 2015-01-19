
BOACLIENT=../boa-client/lib/boa-client.jar
BOAAPI=../boa-client/lib/boa-client-0.1.0.jar
CLASSNAME=ch.usi.inf.sape.boaclient.Main

USER=--user luismastrangelo
PASS=--pass elotrodia
FILE=--input unsafe.boa

unsafe-small.out: unsafe.boa
	java -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) $(USER) $(PASS) --dataset "2013 September (small)" $(FILE) --output unsafe-small.out 

unsafe-large.out: unsafe.boa
	java -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) $(USER) $(PASS) --dataset "2013 September" $(FILE) --output unsafe-large.out
