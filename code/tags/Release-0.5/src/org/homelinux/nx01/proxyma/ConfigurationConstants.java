package org.homelinux.nx01.proxyma;

/**
 * <p>
 * User: makko
 * Date: 30-Apr-2007
 * Time: 10.06.44
 * </p><p>
 * This class is an wrapper for constant values uesd by the
 * configuration servlet of Proxyma rules
 * </p><p>
 * NOTE: this software is released under GPL License.
 *       See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ConfigurationConstants {

    //Requests requred Parameter names
    public static final String method = "method";
    public static final String target = "target";
    public static final String action = "action";

    //Request method parameter valid values
    public static final String updateRule = "updateRule";
    public static final String refreshPage = "refreshPage";
    public static final String addRule = "addRule";
    public static final String importRules = "importRules";
    public static final String exportRules = "exportRules";
    public static final String editRule = "editRule";

    //Request action valid values
    public static final String enable = "enable";
    public static final String disable = "disable";
    public static final String edit = "edit";
    public static final String delete = "delete";
    public static final String ruleList = "ruleList";
    public static final String addNewRule = "addNewRule";
    public static final String modifyRule = "modifyRule";

    //Request rule parameter names
    public static final String proxyFolder = "proxyFolder";
    public static final String proxyPassHost = "proxyPassHost";
    public static final String proxyUser = "proxyUser";
    public static final String proxyPassword = "proxyPassword";
    public static final String newContext = "newContext";
    public static final String maxPostSize = "maxPostSize";
    public static final String javascriptHandlingMode = "javascriptHandlingMode";
    public static final String isRewriteEngineEnabled = "isRewriteEngineEnabled";
    public static final String isRuleEnabled = "isRuleEnabled";
    public static final String loggingPolicy = "loggingPolicy";

    //Request Attribute names
    public static final String ruleCollection = "ruleCollection";
    public static final String ruleBean = "ruleBean";
    public static final String message = "message";
    public static final String proxymaContext = "proxymaContext";
    public static final String reverseProxyServletSubPath = "reverseProxyServletSubPath";

    //Fordard jsp pages
    public static final String editRulePage = "/pages/edit_rule.jsp";
    public static final String listRulesPage = "/pages/list_rules.jsp";

    //Init parameter name
    public static final String httpReverseProxyServletSubPath = "httpReverseProxyServletSubPath";

    //Paths for the logging subsystem into web applications
    public static final String loggingDirectory = "/WEB-INF/logs";
    public static final String loggingConfigurationFilePath = "/WEB-INF/classes";
}
