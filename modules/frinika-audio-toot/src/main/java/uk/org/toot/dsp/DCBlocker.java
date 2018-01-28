package uk.org.toot.dsp;

/*
 * http://www.dspdesignline.com/showArticle.jhtml?articleID=210002082&queryText=freescale
 */

public class DCBlocker
{
	private float a = 0.999f;
	private float p = 0;		// previous m
	
	public float block(float sample) {
		float m = sample + a * p;
		float y = m - p;
		p = m;
		return y;
	}
    
    public void clear() {
        p = 0;
    }
}
