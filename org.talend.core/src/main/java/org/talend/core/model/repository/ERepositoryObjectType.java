// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.core.model.repository;

import org.eclipse.emf.ecore.EObject;
import org.talend.core.i18n.Messages;
import org.talend.core.model.properties.BusinessProcessItem;
import org.talend.core.model.properties.CSVFileConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.DelimitedFileConnectionItem;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LdifFileConnectionItem;
import org.talend.core.model.properties.PositionalFileConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.RegExFileConnectionItem;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.properties.util.PropertiesSwitch;

/**
 * This enum represents all objects types that can be store in the repository.<br/> Exception is the recycle bin that
 * isn't really an object type (could think of moving it).
 * 
 * $Id$
 * 
 */
public enum ERepositoryObjectType {
    BUSINESS_PROCESS("repository.businessProcess"), //$NON-NLS-1$
    PROCESS("repository.process"), //$NON-NLS-1$
    CONTEXT("repository.context"), //$NON-NLS-1$
    ROUTINES("repository.routines"), //$NON-NLS-1$
    SNIPPETS("repository.snippets"), //$NON-NLS-1$
    DOCUMENTATION("repository.documentation"), //$NON-NLS-1$
    METADATA("repository.metadata"), //$NON-NLS-1$
    METADATA_CON_TABLE("repository.metadataTable"), //$NON-NLS-1$
    METADATA_CON_VIEW("repository.metadataView"), //$NON-NLS-1$
    METADATA_CON_SYNONYM("repository.synonym"), //$NON-NLS-1$
    METADATA_CON_QUERY("repository.query"), //$NON-NLS-1$
    METADATA_CONNECTIONS("repository.metadataConnections", "repository.metadataConnections.alias"), //$NON-NLS-1$ //$NON-NLS-2$
    METADATA_FILE_DELIMITED("repository.metadataFileDelimited", "repository.metadataFileDelimited.alias"), //$NON-NLS-1$ //$NON-NLS-2$
    METADATA_FILE_POSITIONAL("repository.metadataFilePositional", "repository.metadataFilePositional.alias"), //$NON-NLS-1$ //$NON-NLS-2$
    METADATA_FILE_REGEXP("repository.metadataFileRegexp", "repository.metadataFileRegexp.alias"), //$NON-NLS-1$ //$NON-NLS-2$
    METADATA_FILE_XML("repository.metadataFileXml", "repository.metadataFileXml.alias"),  //$NON-NLS-1$ //$NON-NLS-2$
    METADATA_FILE_LDIF("repository.metadataFileLdif", "repository.metadataFileLdif.alias"), //$NON-NLS-1$ //$NON-NLS-2$
    FOLDER("repository.folder"), //$NON-NLS-1$
    REFERENCED_PROJECTS("repository.referencedProjects", "repository.referencedProjects.alias"); //$NON-NLS-1$ //$NON-NLS-2$

    private String key;

    private String alias;

    /**
     * Constructor with the i18n key used to display the folder wich contains this object type.
     * 
     * @param key
     */
    ERepositoryObjectType(String key) {
        this.key = key;
    }

    ERepositoryObjectType(String key, String alias) {
        this(key);
        this.alias = alias;
    }

    public String toString() {
        return Messages.getString(getKey());
    }

    public String getAlias() {
        if (alias == null) {
            return null;
        }
        return Messages.getString(alias);
    }

    /**
     * Getter for key.
     * 
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    public static String getFolderName(ERepositoryObjectType type) {
        switch (type) {
        case BUSINESS_PROCESS:
            return "businessProcess"; //$NON-NLS-1$
        case PROCESS:
            return "process"; //$NON-NLS-1$
        case CONTEXT:
            return "context"; //$NON-NLS-1$
        case ROUTINES:
            return "code/routines"; //$NON-NLS-1$
        case DOCUMENTATION:
            return "documentations"; //$NON-NLS-1$
        case METADATA:
            return "metadata"; //$NON-NLS-1$
        case METADATA_CONNECTIONS:
            return "metadata/connections"; //$NON-NLS-1$
        case METADATA_FILE_DELIMITED:
            return "metadata/fileDelimited"; //$NON-NLS-1$
        case METADATA_FILE_POSITIONAL:
            return "metadata/filePositional"; //$NON-NLS-1$
        case METADATA_FILE_REGEXP:
            return "metadata/fileRegex"; //$NON-NLS-1$
        case METADATA_FILE_XML:
            return "metadata/fileXml"; //$NON-NLS-1$
        case METADATA_FILE_LDIF:
            return "metadata/fileLdif"; //$NON-NLS-1$
        default:
            throw new IllegalArgumentException(Messages.getString("ERepositoryObjectType.FolderNotFound",type)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    public static ERepositoryObjectType getItemType(Item item) {
        return (ERepositoryObjectType) new PropertiesSwitch() {

            @Override
            public Object caseFolderItem(FolderItem object) {
                return FOLDER;
            }

            public Object caseDocumentationItem(DocumentationItem object) {
                return DOCUMENTATION;
            }

            public Object caseRoutineItem(RoutineItem object) {
                return ROUTINES;
            }

            public Object caseProcessItem(ProcessItem object) {
                return PROCESS;
            }

            public Object caseContextItem(ContextItem object) {
                return CONTEXT;
            }

            public Object caseBusinessProcessItem(BusinessProcessItem object) {
                return BUSINESS_PROCESS;
            }

            public Object caseCSVFileConnectionItem(CSVFileConnectionItem object) {
                throw new IllegalStateException(Messages.getString("ERepositoryObjectType.NotImplemented")); //$NON-NLS-1$
            }

            public Object caseDatabaseConnectionItem(DatabaseConnectionItem object) {
                return METADATA_CONNECTIONS;
            }

            public Object caseDelimitedFileConnectionItem(DelimitedFileConnectionItem object) {
                return METADATA_FILE_DELIMITED;
            }

            public Object casePositionalFileConnectionItem(PositionalFileConnectionItem object) {
                return METADATA_FILE_POSITIONAL;
            }

            public Object caseRegExFileConnectionItem(RegExFileConnectionItem object) {
                return METADATA_FILE_REGEXP;
            }

            public Object caseXmlFileConnectionItem(XmlFileConnectionItem object) {
                return METADATA_FILE_XML;
            }

            public Object caseLdifFileConnectionItem(LdifFileConnectionItem object) {
                return METADATA_FILE_LDIF;
            }

            public Object defaultCase(EObject object) {
                throw new IllegalStateException();
            }
        }.doSwitch(item);
    }

}
