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

package edu.cmu.cs.stage3.alice.scenegraph.renderer.java3drenderer;

class SceneProxy extends ReferenceFrameProxy {
    private BackgroundProxy m_background = null;
	private javax.media.j3d.VirtualUniverse m_j3dVirtualUniverse = new javax.media.j3d.VirtualUniverse();
	private javax.media.j3d.Locale m_j3dLocale = new javax.media.j3d.Locale( m_j3dVirtualUniverse );
    private javax.media.j3d.BranchGroup m_j3dGroup = new javax.media.j3d.BranchGroup();
	protected javax.media.j3d.Group getJ3DGroup() {
		return m_j3dGroup;
	}
	protected javax.media.j3d.Locale getJ3DLocale() {
		return m_j3dLocale;
	}
    protected void initJ3D() {
        super.initJ3D();
		m_j3dGroup.setCapability( javax.media.j3d.Group.ALLOW_CHILDREN_EXTEND );
		m_j3dGroup.setCapability( javax.media.j3d.Group.ALLOW_CHILDREN_WRITE );
        m_j3dGroup.setUserData( this );
    }
    

	protected void changed( edu.cmu.cs.stage3.alice.scenegraph.Property property, Object value ) {
		if( property == edu.cmu.cs.stage3.alice.scenegraph.Scene.BACKGROUND_PROPERTY ) {
            if( m_background != null ) {
                getJ3DBranchGroup().detach();
                getJ3DBranchGroup().removeChild( m_background.getJ3DBackground() );
            }
            m_background = (BackgroundProxy)getProxyFor( (edu.cmu.cs.stage3.alice.scenegraph.Background)value );
            if( m_background != null ) {
                getJ3DBranchGroup().addChild( m_background.getJ3DBackground() );
                //todo
				m_j3dLocale.addBranchGraph( getJ3DBranchGroup() );
            }
		} else {
			super.changed( property, value );
		}
	}
}