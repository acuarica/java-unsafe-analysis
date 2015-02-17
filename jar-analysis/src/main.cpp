#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <utime.h>

#include <iostream>

#include "unzip.h"

#define WRITEBUFFERSIZE (8192)

#include "jnif.hpp"

using namespace std;
using namespace jnif;

class JavaFile {
public:

	const jnif::u1* const data;
	const int len;
	const jnif::String name;

};

static void analyse(const JavaFile& jf) {
	ClassFile cf(jf.data, jf.len);

	for (ConstPool::Iterator it = cf.iterator(); it.hasNext(); it++) {
		ConstIndex i = *it;
		ConstTag tag = cf.getTag(i);

		const ConstItem* entry = &cf.entries[i];

		switch (tag) {
		case CONST_CLASS:

			if (string(cf.getClassName(i)) == "sun/misc/Unsafe") {
				cout << jf.name << endl;
				//cout << cf;
			}
			break;
		default:
			break;
		}
	}

	for (Method* m : cf.methods) {
		if (!m->hasCode()) {
			continue;
		}

		InstList& instList = m->instList();

		for (Inst* inst : instList) {
			switch (inst->kind) {

			case KIND_INVOKE: {
				ConstIndex mid = inst->invoke()->methodRefIndex;

				String className, name, desc;

				if (cf.getTag(mid) == CONST_INTERMETHODREF) {
					cf.getInterMethodRef(inst->invoke()->methodRefIndex,
							&className, &name, &desc);
				} else {
					cf.getMethodRef(inst->invoke()->methodRefIndex, &className,
							&name, &desc);
				}

				if (className == "sun/misc/Unsafe") {
					const char* methodName = cf.getUtf8(m->nameIndex);
					const char* methodDesc = cf.getUtf8(m->descIndex);

					fprintf(stdout,
							"\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
							jf.name.c_str(), methodName, methodDesc,
							className.c_str(), name.c_str(), desc.c_str());
				}

				break;
			}
			default:
				break;
			}

		}
	}
}

static bool isSuffix(const string& suffix, const string& text) {
	auto res = std::mismatch(suffix.rbegin(), suffix.rend(), text.rbegin());
	return res.first == suffix.rend();
}

static void visitfile(const u1* buffer, int fileSize, const char* filePath) {

	if (!isSuffix(".class", string(filePath))) {
		return;
	}

	JavaFile jf = { buffer, fileSize, filePath };

	try {
		analyse(jf);
	} catch (JnifException& ex) {
		cerr << "Error: Exception on class " << jf.name << endl;
		throw ex;
	}
}

static int doextractcurrentfile(unzFile uf) {
	char filename[256];

	unz_file_info fi;
	int err = unzGetCurrentFileInfo(uf, &fi, filename, sizeof(filename), NULL,
			0, NULL, 0);

	if (err != UNZ_OK) {
		printf("error %d with zipfile in unzGetCurrentFileInfo\n", err);
		return err;
	}

	uInt size_buf = fi.uncompressed_size;
	u1* buf = new u1[size_buf];

	if (buf == NULL) {
		printf("Error allocating memory\n");
		return UNZ_INTERNALERROR;
	}

	err = unzOpenCurrentFile(uf);
	if (err != UNZ_OK) {
		printf("error %d with zipfile in unzOpenCurrentFile\n", err);
	}

	do {
		err = unzReadCurrentFile(uf, buf, size_buf);
		if (err < 0) {
			cerr << "error " << err << " with zipfile in unzReadCurrentFile"
					<< endl;
			break;
		}
	} while (err > 0);

	if (err == UNZ_OK) {
		err = unzCloseCurrentFile(uf);
		if (err == UNZ_OK) {
			visitfile(buf, size_buf, filename);
		} else {
			printf("error %d with zipfile in unzCloseCurrentFile\n", err);
		}
	} else {
		unzCloseCurrentFile(uf); /* don't lose the error */
	}

	delete[] buf;

	return err;
}

static int doextract(unzFile uf) {
	unz_global_info gi;

	int err = unzGetGlobalInfo(uf, &gi);

	if (err != UNZ_OK) {
		printf("error %d with zipfile in unzGetGlobalInfo \n", err);
	}

	for (uLong i = 0; i < gi.number_entry; i++) {
		if (doextractcurrentfile(uf) != UNZ_OK) {
			break;
		}

		if ((i + 1) < gi.number_entry) {
			err = unzGoToNextFile(uf);
			if (err != UNZ_OK) {
				printf("error %d with zipfile in unzGoToNextFile\n", err);
				break;
			}
		}
	}

	return 0;
}

int main(int argc, char *argv[]) {
	if (argc == 1) {
		cerr << "Jar file must be provided." << endl;
		return 1;
	}

	const char * zipfilename = argv[1];
	unzFile uf = unzOpen(zipfilename);

	if (uf == NULL) {
		printf("Cannot open %s or %s.zip\n", zipfilename, zipfilename);
		return 1;
	}

	cerr << "Zip file " << zipfilename << " opened" << endl;

	int ret_value = doextract(uf);

	unzClose(uf);

	return ret_value;
}
