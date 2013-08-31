## Protocol Analyzer API

It is a Protocol Decoder API used to decode protocols in my MultiWork project. The API is based on the abstract class <code>Protocol</code>, to create a new protocol just extends the <code>Protocol</code> class and create the corresponding <code>decode()</code> method. Example:
```java
public class I2CProtocol extends Protocol{

	public I2CProtocol(long freq) {
		super(freq);
	}

	@Override
	public void decode(double startTime) {
		// code
	}
    }
```

The bits which are going to be decoded must be saved on `logicData` member of `Protocol` and the `Strings` with the decoded data must be saved on `mDecodedData` in the class `TimePosition` which define the start and finish time of the decoded event corresponding to the string calculating approximates time based on the sample rate frequency. The `Strings` can easily be added using the `addString()` method.

## Distribution

Jar file can be found on /dist directory.

## License

Project is released under [BSD 2-Clause License](http://opensource.org/licenses/BSD-2-Clause)
