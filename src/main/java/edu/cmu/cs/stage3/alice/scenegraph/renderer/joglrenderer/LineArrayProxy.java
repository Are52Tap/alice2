/*
 * Copyright (c) 1999-2003, Carnegie Mellon University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 * 3. Products derived from the software may not be called "Alice",
 *    nor may "Alice" appear in their name, without prior written
 *    permission of Carnegie Mellon University.
 * 
 * 4. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    "This product includes software developed by Carnegie Mellon University"
 */

package edu.cmu.cs.stage3.alice.scenegraph.renderer.joglrenderer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

class LineArrayProxy extends VertexGeometryProxy {
    
	public void render( RenderContext context ) {
        Integer id = context.getDisplayListID( this );
        if( id == null ) {
            id = context.generateDisplayListID( this );
		    setIsGeometryChanged( true );
        }
        if( isGeometryChanged() ) {
            context.gl2.glNewList( id.intValue(), GL2.GL_COMPILE_AND_EXECUTE );
            context.gl2.glBegin( GL.GL_LINES );
            for( int i=0; i<getNumVertices(); i+=2 ) {
                context.renderVertex( getVertexAt( i ) );
                context.renderVertex( getVertexAt(i+1 ) );
            }
            context.gl2.glEnd();
            context.gl2.glEndList();
		    setIsGeometryChanged( false );
        } else {
            context.gl2.glCallList( id.intValue() );
        }
    }
	
    
	public void pick( PickContext context, boolean isSubElementRequired ) {
        //todo picking
	}    
}
