//Copyright (C) 2007 Steve Taylor.
//Distributed under the Toot Software License, Version 1.0. (See
//accompanying file LICENSE_1_0.txt or copy at
//http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.composition;

import uk.org.toot.music.timing.TimingStrategy;

/**
 * This class is the abstract base class for automated composers.
 * @author st
 *
 */
public abstract class AbstractComposer implements BarComposer
{
	private Context context;

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}
	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	public static class Context
	{
		private TimingStrategy timingStrategy;
		private int level = 64; // default medium level
		private int levelDeviation = 0;
		private float repeatTimingProbability = 0.5f;

		protected int getBipolarDeviation(int deviation) {
			int amount = (int)(Math.random() * (1 + deviation));
			return Math.random() < 0.5 ? amount : -amount;
		}

		public void setTimingStrategy(TimingStrategy strategy) {
			timingStrategy = strategy;
		}
		
		public TimingStrategy getTimingStrategy() {
			return timingStrategy;
		}
		
		public void setRepeatTimingProbability(float probability) {
			repeatTimingProbability = probability;
		}
		
		public float getRepeatTimingProbability() {
			return repeatTimingProbability;
		}
		
		/**
		 * @return the level
		 */
		public int getLevel(int time) {
			int lvl = level;
			if ( levelDeviation != 0 ) {
				lvl += getBipolarDeviation(levelDeviation);
			}
/*			for ( int a = 0; a < 4; a++ ) {
				if ( accentTiming[a] != 0 && (accentTiming[a] & (1 << time)) != 0 ) {
					lvl *= accentFactor[a];
				}
			} */
			if ( lvl < 0 ) lvl = 0;
			else if ( lvl > 127 ) lvl = 127;
			return lvl;
		}

		/**
		 * @param level the level to set
		 */
		public void setLevel(int level) {
			if ( level < 0 || level > 127 ) {
				throw new IllegalArgumentException("require 0 <= level <= 127");
			}
			this.level = level;
		}

		/**
		 * @return the levelDeviation
		 */
		public int getLevelDeviation() {
			return levelDeviation;
		}

		/**
		 * @param levelDeviation the levelDeviation to set
		 */
		public void setLevelDeviation(int levelDeviation) {
			this.levelDeviation = levelDeviation;
		}
	}
}
