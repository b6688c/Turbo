package cc.allio.uno.turbo.common.config;

import cc.allio.uno.turbo.common.mybatis.TurboTenantLineHandler;
import cc.allio.uno.turbo.common.mybatis.handle.BaseChangeMetaObjectHandler;
import cc.allio.uno.turbo.common.mybatis.id.SnowflakeIdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfiguration {

    @Bean
    public BaseChangeMetaObjectHandler baseChangeMetaObjectHandler() {
        return new BaseChangeMetaObjectHandler();
    }

    @Bean
    public SnowflakeIdentifierGenerator snowflakeIdentifierGenerator() {
        return new SnowflakeIdentifierGenerator();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 多租户插件配置,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存万一出现问题
        mybatisPlusInterceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TurboTenantLineHandler()));
        return mybatisPlusInterceptor;
    }

}
