package cn.gan.web.sys.service.impl;

import cn.gan.framework.util.Strings;
import cn.gan.web.sys.bean.SysPerm;
import cn.gan.web.sys.bean.SysRole;
import cn.gan.web.sys.bean.mapper.SysRoleMapper;
import cn.gan.web.sys.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service(value = "sysRoleService")
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public List<SysRole> findAll() {
        return sysRoleMapper.findAll();
    }

    @Override
    public SysRole findById(String id, boolean link, boolean permsTree) {
        SysRole sysRole = null;
        if (link)
            sysRole = sysRoleMapper.findWithLinkById(id);
        else sysRole = sysRoleMapper.findById(id);
        if (link && permsTree){
            sysRole.setPerms(SysPerm.toTrees(sysRole.getPerms()));
        }
        return sysRole;
    }

    @Override
    public int addRole(SysRole sysRole) {
        sysRole.setCreateTime(new Date());
        sysRole.setUpdateTime(new Date());
        sysRole.setCode(Strings.leftComplement((sysRoleMapper.countRoleNumber()+1) + "", '0', 4));
        return sysRoleMapper.insert(sysRole);
    }

    @Override
    public int deleteById(String id) {
        // 首先清除用户与该角色的绑定关系。
        sysRoleMapper.clearUsers(id);
        // 清除权限与该角色的绑定关系。
        sysRoleMapper.clearPerms(id);
        // 删除该角色。
        return sysRoleMapper.delete(id);
    }

    @Override
    public int countByName(String name) {
        return sysRoleMapper.countByName(name);
    }

    @Override
    public int countByNote(String note) {
        return sysRoleMapper.countByNote(note);
    }

    @Override
    public int updateRole(SysRole sysRole) {
        if (sysRole.getUsers() != null) { // 此时代表需要更新该角色下的用户。
            sysRoleMapper.clearUsers(sysRole.getId());
            if (sysRole.getUsers().size() > 0)
                sysRoleMapper.addUsers(sysRole.getId(), sysRole.getUsers());
        }
        if (sysRole.getPerms() != null) { // 此时代表需要更新该角色下的权限。
            sysRoleMapper.clearPerms(sysRole.getId());
            if (sysRole.getPerms().size() > 0)
                sysRoleMapper.addPerms(sysRole.getId(), sysRole.getPerms());
        }
        // 更新角色本身的信息。
        return sysRoleMapper.updateIgnoreNull(sysRole);
    }
}
