deb : init
	@echo "Generating all the debian packages..."
	@bash generateAllDebianPackages.sh ${toolspec}
	@echo " Done"

init :
	@echo "Setting up necessary environment..."
	@if [ ! -d debs ]; then mkdir debs; fi
	@echo " Done"

clean :
	@echo "Deleting unnecessary files..."
	@if [ `ls -lh debs | wc -l` -ne 1 ]; then rm debs/*; fi
	@echo " Done"
