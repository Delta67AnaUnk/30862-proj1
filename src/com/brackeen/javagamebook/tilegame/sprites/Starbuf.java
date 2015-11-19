package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;

import com.brackeen.javagamebook.graphics.*;

public class Starbuf extends Sprite{
	public long life;
	public Starbuf(Animation an,long lf){
		super(an);
		life = lf;
	}
	public Object clone(){
		Constructor constructor = getClass().getConstructors()[0];
        try {
            return constructor.newInstance(new Object[] {
                (Animation)anim.clone(),life
            });
        }
        catch (Exception ex) {
            // should never happen
            ex.printStackTrace();
            return null;
        }
	}
	
	public static class StarB extends Starbuf {
        public StarB(Animation anim,long lf) {
            super(anim, lf);
        }
    }
	
	public static class Expo extends Starbuf {
		public Expo(Animation anim,long lf) {
            super(anim, lf);
        }
	}
	
	public static class Pois extends Starbuf{
		public Pois(Animation anim, long lf){
			super(anim, lf);
		}
	}
	
}
