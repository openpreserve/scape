deb : init
	@echo "Generating all the debian packages..."
	@bash generateAllDebianPackages.sh ${toolspec}
	@echo " Done"

init :
	@echo "Setting up necessary environment..."
	@if [ ! -d debs ]; then mkdir debs; fi
	@if [ ! -d wars ]; then mkdir wars; fi
	@echo " Done"

clean :
	@echo "Deleting unnecessary files..."
	@if [ `ls -lh debs | wc -l` -ne 1 ]; then rm debs/*; fi
	@if [ `ls -lh wars | wc -l` -ne 1 ]; then rm wars/*; fi
	@echo " Done"
