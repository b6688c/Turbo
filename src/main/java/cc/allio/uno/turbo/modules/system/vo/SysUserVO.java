package cc.allio.uno.turbo.modules.system.vo;

import cc.allio.uno.turbo.modules.system.entity.SysOrg;
import cc.allio.uno.turbo.modules.system.entity.SysRole;
import cc.allio.uno.turbo.modules.system.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * sys user值对象
 *
 * @author j.x
 * @date 2023/10/22 12:23
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserVO extends SysUser {

    /**
     * 角色信息
     */
    private List<SysRole> roles;

    /**
     * 组织信息
     */
    private List<SysOrg> orgs;
}
