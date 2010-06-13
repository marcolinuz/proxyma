package org.homelinux.nx01.proxyma.core;

import org.homelinux.nx01.proxyma.beans.ProxymaRuleBean;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import java.util.Collection;
import java.util.Enumeration;

/**
 * <p>
 * User: makko
 * Date: 30-Apr-2007
 * Time: 08.58.54
 * </p><p>
 * This class is a container for the rules of an instance of Proxyma.
 * Note that due to the ProxymaRuleSetsPool you can have many instances of
 * proxyma deployed in the same virtual machine.. all of them could work togheter
 * without problems.
 * </p><p>
 * NOTE: this software is released under GPL License.
 * See the LICENSE of this distribution for more informations.
 * </p>
 *
 * @author Marco Casavecchia Morganti (marcolinuz) [ICQ UIN: 245662445]
 */
public class ProxymaRuleSet {

    /**
     * This is the dafault constructor for this class.
     */
    public ProxymaRuleSet() {
        this.theRealRuleSet = new ConcurrentHashMap();
        this.theRuleSetByProxedHost = new ConcurrentHashMap();
    }

    /**
     * Removes a rule from the ruleset.
     *
     * @param rule the rule to delete from the ruleSet
     * @return true if succeded, otherwise false.
     * @throws NullPointerException if the rule does not exists.
     */
    public boolean removeRule(ProxymaRuleBean rule) throws NullPointerException {
        boolean retValue = true;
        if (theRealRuleSet.containsKey(rule.getProxyFolder())) {
            theRealRuleSet.remove(rule.getProxyFolder());
            theRuleSetByProxedHost.remove(rule.getProxyPassHost());
        } else {
            retValue = false;
            ProxymaLog.instance.errors("WARNING: Can't delete specified rule \"" + rule.getProxyFolder() + "\" because it does not exist.");
            throw new NullPointerException("WARNING: Can't delete specified rule \"" + rule.getProxyFolder() + "\" because it does not exist.");
        }

        return retValue;
    }

    /**
     * Removes the rule that matches the passed key
     * from the ruleset.
     *
     * @param proxyFolder the proxyFolder of the rule to delete from the ruleSet
     * @return true if succeded, otherwise false.
     * @throws NullPointerException if the rule does not exists.
     */
    public boolean removeRule(String proxyFolder) throws NullPointerException {
        ProxymaRuleBean rule = getRule(proxyFolder);
        return removeRule(rule);
    }

    /**
     * Checks and Adds a new rule to the ruleset of the passed proxyma instance,
     * please note that 2 rules for the same folderPath are not allowed.
     *
     * @param rule the rule to add to the pool
     * @return true if succeded, otherwise false.
     * @throws InstantiationException   if the rule already exists
     * @throws IllegalArgumentException if the passed Rule is not valid
     */
    public boolean addNewRule(ProxymaRuleBean rule)
            throws InstantiationException, IllegalArgumentException {
        boolean retValue = true;
        if (theRealRuleSet.containsKey(rule.getProxyFolder())) {
            retValue = false;
            ProxymaLog.instance.errors("WARNING: Can't add specified rule \"" + rule.getProxyFolder() + "\" because it already exists.");
            throw new InstantiationException("WARNING: Can't add specified rule \"" + rule.getProxyFolder() + "\" because it already exists.");
        } else {
            if (theRuleSetByProxedHost.containsKey(rule.getProxyPassHost())) {
                retValue = false;
                ProxymaLog.instance.errors("WARNING: Can't add specified rule \"" + rule.getProxyFolder() + "\" because there is another rule with the same Masqueraded Host as Target.");
                throw new InstantiationException("WARNING: Can't add specified rule \"" + rule.getProxyFolder() + "\" because there is another rule with the same Masqueraded Host as Target.");
            } else {
                ProxymaRuleFactory factory = new ProxymaRuleFactory();
                retValue = factory.isValidRule(rule);
                if (!(retValue)) {
                    ProxymaLog.instance.errors("WARNING: specified rule \"" + rule.toString() + "\" was not added.");
                    throw new IllegalArgumentException("WARNING: specified rule \"" + rule.toString() + "\" was not added.");
                } else {
                    theRealRuleSet.put(rule.getProxyFolder(), rule);
                    theRuleSetByProxedHost.put(rule.getProxyPassHost(), rule);
                }
            }
        }
        return retValue;
    }

    /**
     * Modifies a rule into the ruleset of the passed proxyma instance,
     * note that it works in substitution mode so oldRule is replaced
     * by newRule in the ruleset.
     * <p/>
     * NOTE: if the oldRule was not found it prints a warning message to the server conslole
     * and continue to add the newRule.
     *
     * @param oldRule the old rule (this will be removed)
     * @param newRule the new rule (this will be added)
     * @return true if succeded, otherwise false.
     */
    public boolean updateRule(ProxymaRuleBean oldRule, ProxymaRuleBean newRule)
            throws InstantiationException, IllegalArgumentException {
        boolean retValue = false;
        try {
            removeRule(oldRule);
        } catch (Exception e) {
            ProxymaLog.instance.errors("WARNING: " + e.getMessage());
        } finally {
            retValue = addNewRule(newRule);
        }
        return retValue;
    }

    /**
     * Obtain the specified rule from the internal ConcurrentHashMap.
     *
     * @param proxyFolder the key used to search the rule
     * @return null if the rule was not found
     */
    public ProxymaRuleBean getRule(String proxyFolder) {
        return (ProxymaRuleBean) theRealRuleSet.get(proxyFolder);
    }

    /**
     * Obtain the specified rule from the internal ConcurrentHashMap
     * searching by Proxed Host.
     *
     * @param proxyPassHost the key used to search the rule
     * @return null if the rule was not found
     */
    public ProxymaRuleBean getRuleByProxyPassHost(String proxyPassHost) {
        return (ProxymaRuleBean) theRuleSetByProxedHost.get(proxyPassHost);
    }

    /**
     * Obtain an Enumeration of all the proxyFolders in this ruleset.
     *
     * @return an enumeration of key values.
     */
    public Enumeration getRulesKeyAsEnumeration() {
        return theRealRuleSet.keys();
    }

    /**
     * Obtain a Collection of all the Rules countained in this ruleset.
     *
     * @return a Collection of ProxymaRulesBean.
     */
    public Collection getRulesAsCollection() {
        return theRealRuleSet.values();
    }

    /**
     * Obtain the context where the current HttpReverseProxyServlet is deployed for This RuleSet.
     * It is useful to set the newContext attribute into rules where
     * it was not defined by the user.
     *
     * @return the current default context of the HttpReverseProxyServlet
     */
    public String getProxymaStandardContext() {
        return proxymaStandardContext;
    }

    /**
     * Sets the default context of the HttpReverseProxyServlet for this RuleSet.
     */
    public void setProxymaStandardContext(String proxymaStandardContext) {
        this.proxymaStandardContext = proxymaStandardContext;
    }

    /**
     * The Primary HashMap indexed by proxyFolder
     */
    ConcurrentHashMap theRealRuleSet = null;

    /**
     * Secondary HashMap indexed by proxyPassHost
     */
    ConcurrentHashMap theRuleSetByProxedHost = null;

    /**
     * The Proxyma BasePath (it is used to set the New Context in rules
     * where it was not defined
     */
    String proxymaStandardContext = null;
}
