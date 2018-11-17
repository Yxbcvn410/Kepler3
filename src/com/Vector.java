package com;

import java.math.BigDecimal;

public class Vector {
	
	BigDecimal X;
	BigDecimal Y;
	int x;
	int y;

	public Vector(int _x, int _y) {
		X=new BigDecimal(_x);
		Y=new BigDecimal(y);
		x=_x;
		y=_y;
	}
	
	public Vector(BigDecimal _x, BigDecimal _y)
	{
		X=_x;
		Y=_y;
		x=X.intValue();
		y=Y.intValue();
	}
	
	void Apply(Vector p)
	{
		X=this.X.add(p.X);
		Y=this.Y.add(p.Y);
		x=X.intValue();
		y=Y.intValue();
	}
	
	void Multiply(BigDecimal n)
	{
		X=this.X.multiply(n);
		Y=this.Y.multiply(n);
		x=X.intValue();
		y=Y.intValue();
	}
	
	public Vector clone()
	{
		return new Vector(this.X, this.Y);
	}
}
