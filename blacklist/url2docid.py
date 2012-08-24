#!/usr/bin/python
# coding=utf-8
import urllib2

def get_url_sign(url, server="http://10.115.88.58:1111/?norm_query="):
    """
    根据query查询query的签名
    """
    if type(url)==unicode:
      url=url.encode("utf-8")
    res = urllib2.urlopen(u'http://10.115.88.58:1111?norm_url='+urllib2.quote(url))
    norm = res.read()
    res.close()

    if not norm:
      raise Exception('fail to normalize!')

    if type(norm)==unicode:
      norm=norm.encode("utf-8")
    res = urllib2.urlopen(u'http://10.115.88.58:1111?sign_url='+urllib2.quote(norm))
    sign = res.read()
    res.close()

    return sign

if __name__ == '__main__':
    import sys
    if len(sys.argv) < 2:
        exit()
    print get_url_sign(sys.argv[1])

