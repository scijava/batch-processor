// Let's take an example script that takes four inputs:

// * a File input
#@ File (label = "Input file", style = "file, extensions:tif/tiff") inputFile

// * a String choice
#@ String (label = "What to measure?", choices = {"Mean", "Min", "Max"}) measurement

// * two Service parameters that will be auto-filled
#@ IOService ioService
#@ OpService ops

// and defines two outputs
#@output String condition
#@output resultValue

// Read the dataset from the given file
dataset = ioService.open(inputFile.getPath())

// Extract the parent folder name as condition
m = inputFile.getPath() =~ $/.*/([^/]+)/[^/]*/$
condition = m[0][1]

// Compute desired statistics
resultValue = ops.run("stats.${measurement.toLowerCase()}", dataset).get()
