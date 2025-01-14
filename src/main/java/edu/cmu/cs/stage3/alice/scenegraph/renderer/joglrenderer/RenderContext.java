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
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;

class RenderContext extends Context {
    private RenderTarget m_renderTarget;

    private int m_lastTime_nextLightID = GLLightingFunc.GL_LIGHT0;
    private int m_nextLightID;
    private boolean m_isFogEnabled;
    private boolean m_renderOpaque;
    
    private float[] m_ambient = new float[ 4 ];
    private java.nio.FloatBuffer m_ambientBuffer = java.nio.FloatBuffer.wrap( m_ambient );

    private java.util.Hashtable m_displayListMap = new java.util.Hashtable();
    private java.util.Hashtable m_textureBindingMap = new java.util.Hashtable();
    private TextureMapProxy m_currTextureMapProxy;

    private boolean m_isShadingEnabled;
    
    private java.awt.Rectangle m_clearRect = new java.awt.Rectangle();

    public RenderContext( RenderTarget renderTarget ) {
        m_renderTarget = renderTarget;
        m_renderOpaque = true;
    }
    
	public void init( GLAutoDrawable drawable ) {
    	super.init( drawable );
        forgetAllTextureMapProxies();
        forgetAllGeometryProxies();
    }
    
	public void display( GLAutoDrawable drawable ) {
        super.display( drawable );
        
        m_renderTarget.commitAnyPendingChanges();
        m_clearRect.setBounds( 0, 0, 0, 0 );
        m_renderTarget.performClearAndRenderOffscreen( this );

		if( m_clearRect.x == 0 && m_clearRect.y == 0 && m_clearRect.width == m_width && m_clearRect.height == m_height ) {
            //pass
        } else {
            gl2.glEnable( GL.GL_SCISSOR_TEST );
            gl2.glClearColor( 0, 0, 0, 1 );
            try {
                if( m_clearRect.x > 0 ) {
                    gl2.glScissor( 0, 0, m_clearRect.x, m_height );
                    gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
                }
                if( ( m_clearRect.x + m_clearRect.width ) < m_width ) {
                    gl2.glScissor( m_clearRect.x + m_clearRect.width, 0, m_width - m_clearRect.width, m_height );
                    gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
                }
                if( m_clearRect.y > 0 ) {
                    gl2.glScissor( 0, 0, m_width, m_clearRect.y );
                    gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
                }
                if( ( m_clearRect.y + m_clearRect.height ) < m_height ) {
                    gl2.glScissor( 0, m_clearRect.y + m_clearRect.height, m_width, m_height - m_clearRect.height );
                    gl2.glClear( GL.GL_COLOR_BUFFER_BIT );
                }
            } finally { 
                gl2.glDisable( GL.GL_SCISSOR_TEST );
            }
        }
	}

    public void beginAffectorSetup() {
        m_ambient[ 0 ] = 0;
        m_ambient[ 1 ] = 0;
        m_ambient[ 2 ] = 0;
        m_ambient[ 3 ] = 1;
        m_nextLightID = GLLightingFunc.GL_LIGHT0;
        
        m_isFogEnabled = false;

        m_currTextureMapProxy = null;
    }

    public void endAffectorSetup() {
        gl2.glLightModelfv( GL2ES1.GL_LIGHT_MODEL_AMBIENT, m_ambientBuffer );
        for( int id=m_nextLightID; id<m_lastTime_nextLightID; id++ ) {
            gl2.glDisable( id );
        }
        //gl2.glDisable( GL.GL_LIGHT0 );
        if( m_isFogEnabled ) {
            //System.err.println( "fog on" );
            gl2.glEnable( GL2ES1.GL_FOG );
        } else {
            //System.err.println( "fog off" );
            gl2.glDisable( GL2ES1.GL_FOG );
        }

        //todo?
/*	    if( m_currMaxLightID == INIT_LIGHT_ID ) {
	        gl2.glDisable( GL.GL_LIGHTING );
	    } else {
	        gl2.glEnable( GL.GL_LIGHTING );
	    } */
        m_lastTime_nextLightID = m_nextLightID;

        gl2.glEnable( GL.GL_DEPTH_TEST );
        gl2.glEnable( GL.GL_CULL_FACE );
        gl2.glCullFace( GL.GL_BACK ); 
    }
    
    public void clear( BackgroundProxy backgroundProxy, java.awt.Rectangle viewport ) {
        gl2.glViewport( viewport.x, viewport.y, viewport.width, viewport.height );
        if( backgroundProxy != null ) {
            backgroundProxy.clear( this );
        }
        if( m_clearRect.width == 0 || m_clearRect.height == 0 ) {
            m_clearRect.setBounds( viewport );
        } else {
            m_clearRect.union( viewport );
        }
        //m_clearRect.setBounds( 0, 0, m_width, m_height );
    }
    
    public boolean isFogEnabled() {
        return m_isFogEnabled;
    }
    
    public void setRenderTransparent(){
    	m_renderOpaque = false;
    }

    public void setRenderOpaque(){
    	m_renderOpaque = true;
    }

    public boolean renderOpaque(){
    	return m_renderOpaque;
    }   
    
    public void setIsFogEnabled( boolean isFogEnabled ) {
        m_isFogEnabled = isFogEnabled;
    }
    
    public void addAmbient( float[] color ) {
        m_ambient[ 0 ] += color[ 0 ];
        m_ambient[ 1 ] += color[ 1 ];
        m_ambient[ 2 ] += color[ 2 ];
    }
    
    public int getNextLightID() {
        int id = m_nextLightID;
        //System.err.println( "getNextLightID: " + id + " " + GL.GL_LIGHT0 );
        m_nextLightID ++;
        return id;
    }
    
    public Integer getDisplayListID( GeometryProxy geometryProxy ) {
        return (Integer)m_displayListMap.get( geometryProxy );
    }
    
    public Integer generateDisplayListID( GeometryProxy geometryProxy ) {
		Integer id = new Integer( gl2.glGenLists( 1 ) );
		m_displayListMap.put( geometryProxy, id );
        return id;
    }

    private void forgetAllGeometryProxies() {
        java.util.Enumeration enum0 = m_displayListMap.keys();
        while( enum0.hasMoreElements() ) {
            forgetGeometryProxy( (GeometryProxy)enum0.nextElement(), false );
        }
        m_displayListMap.clear();
    }
    
    public void forgetGeometryProxy( GeometryProxy geometryProxy, boolean removeFromMap ) {
        Integer value = (Integer)m_displayListMap.get( geometryProxy );
        if( value != null ) {
	        gl2.glDeleteLists( value.intValue(), 1 );
	        if( removeFromMap ) {
	            m_displayListMap.remove( geometryProxy );
	        }
        } else {
            //todo?
        }
    }
    
    public void forgetGeometryProxy( GeometryProxy geometryProxy ) {
        forgetGeometryProxy( geometryProxy, true );
    }

    private void forgetAllTextureMapProxies() {
        java.util.Enumeration enum0 = m_textureBindingMap.keys();
        while( enum0.hasMoreElements() ) {
            forgetTextureMapProxy( (TextureMapProxy)enum0.nextElement(), false );
        }
        m_textureBindingMap.clear();
    }
    
    public void forgetTextureMapProxy( TextureMapProxy textureMapProxy, boolean removeFromMap ) {
        Integer value = (Integer)m_textureBindingMap.get( textureMapProxy );
        if( value != null ) {
        	int id = value.intValue();
	        java.nio.IntBuffer atID = java.nio.IntBuffer.allocate( 1 );
	        atID.put( id );
	        atID.rewind();
	        gl2.glDeleteTextures( atID.limit(), atID );
	        if( removeFromMap ) {
	            m_textureBindingMap.remove( textureMapProxy );
	        }
        } else {
            //todo?
        }
//        Integer value = (Integer)m_textureBindingMap.get( textureMapProxy );
//        if( value != null ) {
//	        int[] atID = { value.intValue() };
//	        gl.glDeleteTextures( atID.length, atID );
//	        if( removeFromMap ) {
//	            m_textureBindingMap.remove( textureMapProxy );
//	        }
//        } else {
//            //todo?
//        }
    }
    
    public void forgetTextureMapProxy( TextureMapProxy textureMapProxy ) {
        forgetTextureMapProxy( textureMapProxy, true );
    }
    
    public void setTextureMapProxy( TextureMapProxy textureMapProxy ) {
        if( textureMapProxy != null && textureMapProxy.isImageSet() ) {
            gl2.glEnable( GL.GL_TEXTURE_2D );
            if( m_currTextureMapProxy != textureMapProxy ) {
                if( textureMapProxy != null ) {
                    Integer value = (Integer)m_textureBindingMap.get( textureMapProxy );
                    if( textureMapProxy.prepareByteBufferIfNecessary() || value == null ) {
                    	if( value == null ) {
    	        	        java.nio.IntBuffer atID = java.nio.IntBuffer.allocate( 1 );
    	                    gl2.glGenTextures( atID.limit(), atID );
    	                    value = new Integer( atID.get() );
    	                    m_textureBindingMap.put( textureMapProxy, value );
                    	}
            			//System.err.println( "BIND: " + value.intValue() + " " + textureMapProxy );
                        gl2.glBindTexture( GL.GL_TEXTURE_2D, value.intValue() );
                        int internalFormat;
                        int format;
                        if( textureMapProxy.isPotentiallyAlphaBlended() ) {
                        	internalFormat = GL.GL_RGBA;
                        	format = GL.GL_RGBA;
                        } else {
                        	internalFormat = GL.GL_RGB;
                        	format = GL.GL_RGB;
                        }
                        java.nio.ByteBuffer pixels = textureMapProxy.getPixels();
                        //PrintUtilities.print( System.err, pixels );
                        //System.err.println( pixels );
                       
                        gl2.glTexImage2D( GL.GL_TEXTURE_2D, 0, internalFormat, textureMapProxy.getWidthPowerOf2(), textureMapProxy.getHeightPowerOf2(), 0, format, GL.GL_UNSIGNED_BYTE, pixels );
                        gl2.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
                        gl2.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
                        gl2.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
                        gl2.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
                    } else {
                        gl2.glBindTexture( GL.GL_TEXTURE_2D, value.intValue() );
                    }
                }
                m_currTextureMapProxy = textureMapProxy;
            }
        } else {
            gl2.glDisable( GL.GL_TEXTURE_2D );
        }
//        if( textureMapProxy != null ) {
//            gl.glEnable( GL.GL_TEXTURE_2D );
//        } else {
//            gl.glDisable( GL.GL_TEXTURE_2D );
//        }
//        if( m_currTextureMapProxy != textureMapProxy ) {
//            if( textureMapProxy != null ) {
//                if( textureMapProxy.isChanged() ) {
//                    forgetTextureMapProxy( textureMapProxy );
//                }
////                Integer value = (Integer)m_textureBindingMap.get( textureMapProxy );
////                if( value == null ) {
////        	        java.nio.IntBuffer atID = java.nio.IntBuffer.allocate( 1 );
////                    gl.glGenTextures( atID.limit(), atID );
////                    value = new Integer( atID.get() );
////                    m_textureBindingMap.put( textureMapProxy, value );
////                    gl.glBindTexture( GL.GL_TEXTURE_2D, value.intValue() );
////                    gl.glTexImage2D( GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, textureMapProxy.getWidthPowerOf2(), textureMapProxy.getHeightPowerOf2(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, textureMapProxy.getPixels() );
////                    gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
////                    gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
////                    gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR );
////                    gl.glTexParameterf( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR );
////                    textureMapProxy.setIsChanged( false );
////                } else {
////                    gl.glBindTexture( GL.GL_TEXTURE_2D, value.intValue() );
////                }
//            }
//            m_currTextureMapProxy = textureMapProxy;
//        }
    }

    public boolean isShadingEnabled() {
        return m_isShadingEnabled;
    }
    
    public void setIsShadingEnabled( boolean isShadingEnabled ) {
        m_isShadingEnabled = isShadingEnabled;
        if( m_isShadingEnabled ) {
            gl2.glEnable( GLLightingFunc.GL_LIGHTING );
        } else {
        	gl2.glDisable( GLLightingFunc.GL_LIGHTING );
        }
    }
    
    protected void renderVertex( edu.cmu.cs.stage3.alice.scenegraph.Vertex3d vertex ) {
    	
    	if( m_currTextureMapProxy != null  && vertex.textureCoordinate0!=null) {  		
	        double u = m_currTextureMapProxy.mapU( vertex.textureCoordinate0.x );
	        double v = m_currTextureMapProxy.mapV( vertex.textureCoordinate0.y );
			gl2.glTexCoord2d( u, v );			
	    }
    	    
    	if (vertex.diffuseColor!=null ){    
    		gl2.glColor4f(vertex.diffuseColor.red, vertex.diffuseColor.green, vertex.diffuseColor.blue, vertex.diffuseColor.alpha);	
    	}	
            
 		if( m_isShadingEnabled ) {
 			gl2.glNormal3d( vertex.normal.x, vertex.normal.y, -vertex.normal.z );
        }	
		gl2.glVertex3d( vertex.position.x, vertex.position.y, -vertex.position.z );
	}

	public void dispose(GLAutoDrawable drawable) { }
}
