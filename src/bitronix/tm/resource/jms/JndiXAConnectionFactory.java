package bitronix.tm.resource.jms;

import javax.jms.XAConnectionFactory;
import javax.jms.XAConnection;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.util.Hashtable;

/**
 * {@link XAConnectionFactory} implementation that wraps another {@link XAConnectionFactory} implementation available
 * in some JNDI tree.
 * <p>&copy; Bitronix 2005, 2006, 2007</p>
 *
 * @author lorban
 */
public class JndiXAConnectionFactory implements XAConnectionFactory {

    private String initialContextFactory;
    private String providerUrl;
    private String name;
    private String securityPrincipal;
    private String securityCredentials;
    private XAConnectionFactory wrappedFactory;


    public JndiXAConnectionFactory() {
    }

    /**
     * The {@link Context#INITIAL_CONTEXT_FACTORY} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * @return the {@link Context#INITIAL_CONTEXT_FACTORY} value.
     */
    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    /**
     * Set the {@link Context#INITIAL_CONTEXT_FACTORY} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * If not set, the {@link Context} is created without the environment parameter, using the default constructor.
     * @param initialContextFactory the {@link Context#INITIAL_CONTEXT_FACTORY} value.
     */
    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    /**
     * The {@link Context#PROVIDER_URL} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * @return the {@link Context#PROVIDER_URL} value.
     */
    public String getProviderUrl() {
        return providerUrl;
    }

    /**
     * Set the {@link Context#PROVIDER_URL} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * If not set, the {@link Context} is created without the environment parameter, using the default constructor.
     * @param providerUrl the {@link Context#PROVIDER_URL} value.
     */
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    /**
     * The {@link Context#SECURITY_PRINCIPAL} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * @return the {@link Context#SECURITY_PRINCIPAL} value.
     */
    public String getSecurityPrincipal() {
        return securityPrincipal;
    }

    /**
     * Set the {@link Context#SECURITY_PRINCIPAL} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * If {@link Context#INITIAL_CONTEXT_FACTORY} and {@link Context#PROVIDER_URL} are not set, this value is ignored.
     * @param securityPrincipal the {@link Context#SECURITY_PRINCIPAL} value.
     */
    public void setSecurityPrincipal(String securityPrincipal) {
        this.securityPrincipal = securityPrincipal;
    }

    /**
     * The {@link Context#SECURITY_CREDENTIALS} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * @return the {@link Context#SECURITY_CREDENTIALS} value.
     */
    public String getSecurityCredentials() {
        return securityCredentials;
    }

    /**
     * Set the {@link Context#SECURITY_CREDENTIALS} of the JNDI {@link Context} used to fetch the {@link XAConnectionFactory}.
     * If {@link Context#INITIAL_CONTEXT_FACTORY} and {@link Context#PROVIDER_URL} are not set, this value is ignored.
     * @param securityCredentials the {@link Context#SECURITY_CREDENTIALS} value.
     */
    public void setSecurityCredentials(String securityCredentials) {
        this.securityCredentials = securityCredentials;
    }

    /**
     * The JNDI name under which the {@link XAConnectionFactory} is available.
     * @return The JNDI name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the JNDI name under which the {@link XAConnectionFactory} is available.
     * @param name the JNDI name.
     */
    public void setName(String name) {
        this.name = name;
    }

    protected void init() throws NamingException {
        if (wrappedFactory != null)
            return;

        Context ctx;
        if (!isEmpty(initialContextFactory) && !isEmpty(providerUrl)) {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            env.put(Context.PROVIDER_URL, providerUrl);
            if (!isEmpty(securityPrincipal))
                env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
            if (!isEmpty(securityCredentials))
                env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
            ctx = new InitialContext(env);
        }
        else {
            ctx = new InitialContext();
        }

        wrappedFactory = (XAConnectionFactory) PortableRemoteObject.narrow(ctx.lookup(name), XAConnectionFactory.class);
    }

    public XAConnection createXAConnection() throws JMSException {
        try {
            init();
            return wrappedFactory.createXAConnection();
        } catch (NamingException ex) {
            throw (JMSException) new JMSException("error looking up wrapped XAConnectionFactory at " + name).initCause(ex);
        }
    }

    public XAConnection createXAConnection(String userName, String password) throws JMSException {
        try {
            init();
            return wrappedFactory.createXAConnection(userName, password);
        } catch (NamingException ex) {
            throw (JMSException) new JMSException("error looking up wrapped XAConnectionFactory at " + name).initCause(ex);
        }
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().equals("");
    }

}