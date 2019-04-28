package org.hunter.pocket.session;

import org.hunter.pocket.session.actions.BuildDictionary;
import org.hunter.pocket.session.actions.BuildTransaction;
import org.hunter.pocket.session.actions.OperateDictionary;

/**
 * @author wujianchuan 2018/12/31
 */

public interface Session extends BuildDictionary, OperateDictionary, BuildTransaction {
    //TODO 缓存实现方式  待定
}
