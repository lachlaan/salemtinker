/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.Color;
import java.awt.Font;
import static java.lang.Math.PI;

public class FlowerMenu extends Widget {
    public static final Tex pbgl = Resource.loadtex("gfx/hud/fpl");
    public static final Tex pbgm = Resource.loadtex("gfx/hud/fpm");
    public static final Tex pbgr = Resource.loadtex("gfx/hud/fpr");
    static Color ptc = new Color(248, 240, 193);
    static Text.Foundry ptf = new Text.Foundry(new Font("SansSerif", Font.PLAIN, 12));
    static int ph = pbgm.sz().y, ppl = 8;
    Petal[] opts;
	
    static {
	Widget.addtype("sm", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    if((c.x == -1) && (c.y == -1))
			c = parent.ui.lcc;
		    String[] opts = new String[args.length];
		    for(int i = 0; i < args.length; i++)
			opts[i] = (String)args[i];
		    return(new FlowerMenu(c, parent, opts));
		}
	    });
    }
	
    public class Petal extends Widget {
	public String name;
	public double ta, tr;
	public int num;
	Tex text;
	double a = 1;
		
	public Petal(String name) {
	    super(Coord.z, Coord.z, FlowerMenu.this);
	    this.name = name;
	    text = new TexI(Utils.outline2(ptf.render(name, ptc).img, Utils.contrast(ptc)));
	    sz = new Coord(text.sz().x + 25, ph);
	}
		
	public void move(Coord c) {
	    this.c = c.add(sz.div(2).inv());
	}
		
	public void move(double a, double r) {
	    move(Coord.sc(a, r));
	}
		
	public void draw(GOut g) {
	    g.chcolor(255, 255, 255, (int)(255 * a));
	    g.image(pbgl, Coord.z);
	    g.image(pbgm, new Coord(pbgl.sz().x, 0), new Coord(sz.x - pbgl.sz().x - pbgr.sz().x, sz.y));
	    g.image(pbgr, new Coord(sz.x - pbgr.sz().x, 0));
	    g.image(text, sz.div(2).add(text.sz().div(2).inv()));
	}
		
	public boolean mousedown(Coord c, int button) {
	    choose(this);
	    return(true);
	}
    }
	
    public class Opening extends NormAnim {
	Opening() {super(0.25);}
	
	public void ntick(double s) {
	    for(Petal p : opts) {
		p.move(p.ta, p.tr * (2 - s));
		p.a = s;
	    }
	}
    }
	
    public class Chosen extends NormAnim {
	Petal chosen;
		
	Chosen(Petal c) {
	    super(0.75);
	    chosen = c;
	}
		
	public void ntick(double s) {
	    for(Petal p : opts) {
		if(p == chosen) {
		    if(s > 0.6) {
			p.a = 1 - ((s - 0.6) / 0.4);
		    } else if(s < 0.3) {
			p.move(p.ta, p.tr * (1 - (s / 0.3)));
			p.a = 1;
		    }
		} else {
		    if(s > 0.3) {
			p.a = 0;
		    } else {
			p.a = 1 - (s / 0.3);
			p.move(p.ta - (s * PI), p.tr);
		    }
		}
	    }
	    if(s == 1.0)
		ui.destroy(FlowerMenu.this);
	}
    }
	
    public class Cancel extends NormAnim {
	Cancel() {super(0.25);}

	public void ntick(double s) {
	    for(Petal p : opts) {
		p.move(p.ta, p.tr * (1 + s));
		p.a = 1 - s;
	    }
	    if(s == 1.0)
		ui.destroy(FlowerMenu.this);
	}
    }
	
    private static void organize(Petal[] opts) {
	int l = 1, p = 0, i = 0;
	int lr = -1;
	for(i = 0; i < opts.length; i++) {
	    if(lr == -1) {
		//lr = (int)(ph / (1 - Math.cos((2 * PI) / (ppl * l))));
		lr = 75 + (50 * (l - 1));
	    }
	    opts[i].ta = (PI / 2) - (p * (2 * PI / (l * ppl)));
	    opts[i].tr = lr;
	    if(++p >= (ppl * l)) {
		l++;
		p = 0;
		lr = -1;
	    }
	}
    }
	
    public FlowerMenu(Coord c, Widget parent, String... options) {
	super(c, Coord.z, parent);
	opts = new Petal[options.length];
	for(int i = 0; i < options.length; i++) {
	    opts[i] = new Petal(options[i]);
	    opts[i].num = i;
	}
	organize(opts);
	ui.grabmouse(this);
	ui.grabkeys(this);
	new Opening();
    }
	
    public boolean mousedown(Coord c, int button) {
	if(!anims.isEmpty())
	    return(true);
	if(!super.mousedown(c, button))
	    choose(null);
	return(true);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "cancel") {
	    new Cancel();
	    ui.grabmouse(null);
	    ui.grabkeys(null);
	} else if(msg == "act") {
	    new Chosen(opts[(Integer)args[0]]);
	    ui.grabmouse(null);
	    ui.grabkeys(null);
	}
    }
	
    public void draw(GOut g) {
	super.draw(g, false);
    }
    
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if((key >= '0') && (key <= '9')) {
	    int opt = (key == '0')?10:(key - '1');
	    if(opt < opts.length)
		choose(opts[opt]);
	    ui.grabkeys(null);
	    return(true);
	} else if(key == 27) {
	    choose(null);
	    ui.grabkeys(null);
	    return(true);
	}
	return(false);
    }
    
    public void choose(Petal option) {
	if(option == null) {
	    wdgmsg("cl", -1);
	} else {
	    wdgmsg("cl", option.num, ui.modflags());
	}
    }
}
