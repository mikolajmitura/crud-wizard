package pl.jalokim.crudwizard.datastorage.jdbc;

import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

@Component
public class JdbcDataStorageBeansLazyProvider implements BeanFactoryAware {

    private static final AtomicReference<JdbcDataStorageBeansLazyProvider> JDBC_DATA_STORAGE_LAZY_PROVIDER_INSTANCE = new AtomicReference<>();

    private BeanFactory beanFactory;

    public static AtomicReference<JdbcDataStorageBeansLazyProvider> getReferenceProvider() {
        return JDBC_DATA_STORAGE_LAZY_PROVIDER_INSTANCE;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        JDBC_DATA_STORAGE_LAZY_PROVIDER_INSTANCE.set(this);
    }

    public DataSource getDataSource(String beanName) {
        return getBeanOrPrimary(beanName, DataSource.class);
    }

    public JdbcDataStorageDataSourceProperties getDataSourceProperties(String beanName) {
        return getBeanOrPrimary(beanName, JdbcDataStorageDataSourceProperties.class);
    }

    private <T> T getBeanOrPrimary(String beanName, Class<T> beanType) {
        try {
            return beanFactory.getBean(beanName, beanType);
        } catch (BeansException ex) {
            return beanFactory.getBean(beanType);
        }
    }
}
