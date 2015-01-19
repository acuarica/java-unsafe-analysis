
BOACLIENT=../boa-client/lib/boa-client.jar
BOAAPI=../boa-client/lib/boa-client-0.1.0.jar
CLASSNAME=ch.usi.inf.sape.boaclient.Main

USER=--user luismastrangelo
PASS=--pass elotrodia
FILE=--file unsafe.boa

small:
	java -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) $(USER) $(PASS) $(FILE) --dataset "2013 September (small)"

large:
	java -cp $(BOACLIENT):$(BOAAPI) $(CLASSNAME) $(USER) $(PASS) $(FILE) --dataset "2013 September"
