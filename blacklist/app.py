import logging
import urllib2
from tornado import web
import tornado.ioloop
from tornado.options import define, options
import tornado.web
import os
import url2docid


hostnames=[
    'i676.dong.shgt.qihoo.net',
    'i677.dong.shgt.qihoo.net',
    'i678.dong.shgt.qihoo.net',
    'i679.dong.shgt.qihoo.net',
    'i680.dong.shgt.qihoo.net',
    'i681.dong.shgt.qihoo.net',
    'i682.dong.shgt.qihoo.net',
    'i683.dong.shgt.qihoo.net',
    'i684.dong.shgt.qihoo.net',
    'i685.dong.shgt.qihoo.net',
    'i686.dong.shgt.qihoo.net',
    'i687.dong.shgt.qihoo.net',
    'i688.dong.shgt.qihoo.net',
    'i689.dong.shgt.qihoo.net',
    'i690.dong.shgt.qihoo.net',
    'i691.dong.shgt.qihoo.net',
    'i692.dong.shgt.qihoo.net',
    'i693.dong.shgt.qihoo.net',
    'i694.dong.shgt.qihoo.net',
    'i695.dong.shgt.qihoo.net'
]

port=8081

class BlacklistHandler(tornado.web.RequestHandler):
    def get(self):
        op = self.get_argument("op")
        if op == 'add':
            docid = long(self.get_argument("docid",None))
            if docid != None:
                #print 'adding %s to blacklist' % docid
                idx = docid%len(hostnames)
                realDocId = docid/len(hostnames)
                cmd = 'http://%s:%d/%s/?op=blacklist' % (hostnames[idx],port,realDocId)
                logging.info(cmd)
                urllib2.urlopen(cmd)
                os.system(cmd)
        elif op == 'del':
            docid = long(self.get_argument("docid",None))
            if docid != None:
                #print 'removing %s from blacklist' % docid
                idx = (docid)%len(hostnames)
                realDocId = docid/len(hostnames)
                cmd = 'http://%s:%d/%s/?op=whitelist' % (hostnames[idx],port,realDocId)
                logging.info(cmd)
                urllib2.urlopen(cmd)
                os.system(cmd)
        elif op == 'clear':
            logging.warning('warning! clear all blacklist')
            for h in hostnames:
                cmd = 'http://%s:%d/0/?op=clear' % (h,port)
                logging.warning( cmd)
                urllib2.urlopen(cmd)
                os.system(cmd)
        elif op == 'log':
            action = self.get_argument("action","blacklist")
            url = self.get_argument("url",None)
            if url != None:
                logging.info(str(action)+ ' url: ' + str(url))
#        self.write("OK")
        self.finish()
class Url2DocidHandler(tornado.web.RequestHandler):
    def post(self):
        url = self.get_argument('url',None)
        if url != None:
            print url
            id = url2docid.get_url_sign(url)
            self.write(str(id))
        else:
            self.write('Url is null')
        self.finish()

application = tornado.web.Application([
    (r"/blacklist", BlacklistHandler),
    (r"/url2docid", Url2DocidHandler),
    (r"/static/(.*)", tornado.web.StaticFileHandler, {"path": "static"}),
])

if __name__ == "__main__":
    define("port", default=18888, help="run on the given port", type=int)
    tornado.options.parse_command_line()
    application.listen(options.port)
    logging.info('blacklist server start listening %s...' % options.port)
    tornado.ioloop.IOLoop.instance().start()