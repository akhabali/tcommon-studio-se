// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.resource;

import org.talend.core.IService;
import org.talend.core.language.ECodeLanguage;

/**
 * yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 * 
 */
public interface IResourceService extends IService {

    public String getJavaLibraryPath();

    public String getResourcesPath();

    // MOD gdbu 2011-5-10 bug : 21138
    public String getDemoDescription(ECodeLanguage language, String projectname);

    /**
     * DOC ycbai Comment method "getAntScriptFilePath".
     * 
     * Get the path of ant script template file.
     * 
     * @return
     */
    public String getAntScriptFilePath();

    /**
     * DOC ycbai Comment method "getMavenScriptFilePath".
     * 
     * Get the path of maven script template file.
     * 
     * @return
     */
    public String getMavenScriptFilePath();

    /**
     * DOC ycbai Comment method "getMavenAssemblyFilePath".
     * 
     * get the path of maven assembly template file.
     * 
     * @return
     */
    public String getMavenAssemblyFilePath();

}
