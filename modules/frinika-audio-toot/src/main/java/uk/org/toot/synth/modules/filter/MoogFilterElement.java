package uk.org.toot.synth.modules.filter;

//http://musicdsp.org/archive.php?classid=3#26
public class MoogFilterElement
{
	private double in1, in2, in3, in4, out1, out2, out3, out4;

	public float filter(float input, float fc, float res) {
		double f = fc * 1.16;
		double fb = res * (1.0 - 0.15 * f * f);
		input -= out4 * fb;
		input *= 0.35013 * (f*f)*(f*f);
		out1 = input + 0.3 * in1 + (1 - f) * out1; // Pole 1
		in1  = input;
		out2 = out1 + 0.3 * in2 + (1 - f) * out2;  // Pole 2
		in2  = out1;
		out3 = out2 + 0.3 * in3 + (1 - f) * out3;  // Pole 3
		in3  = out2;
		out4 = out3 + 0.3 * in4 + (1 - f) * out4;  // Pole 4
		in4  = out3;
		return (float)out4;	
	}

}
