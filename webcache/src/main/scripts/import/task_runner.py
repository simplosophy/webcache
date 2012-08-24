from threading import Thread, Event, Lock, Condition, BoundedSemaphore
import sys

__all__ = ["map_task"]

__version__ = '1.0'

class task(object):
  def __init__(self, func, semaphore):
    self.__func = func
    self.__thread = Thread(target=self.run)
    self.__thread.daemon = True

    self.__start_event = Event()
    self.__start_event.clear()

    self.__terminate_event = Event()
    self.__terminate_event.clear()

    self.__terminated = False
    self.__started = False
    self.__semaphore = semaphore
    self.__thread.start()

  @property
  def started(self):
    return self.__started

  @started.setter
  def started(self, s):
    self.__started = s

  @property
  def data(self):
    return self.__data

  @data.setter
  def data(self, d):
    self.__data = d

  def run(self):
    while True:
#      print >> sys.stderr, 'waiting for start event'
      self.__start_event.wait()
      self.__start_event.clear()
      self.__semaphore.acquire()

      if self.__started:
        self.__terminate_event.clear()

        try:
#          print >> sys.stderr, 'before func'
          self.__func(self.__data)
#          print >> sys.stderr, 'after func'
        except:
          import traceback

          traceback.print_exc()

        self.__semaphore.release()
        self.__started = False

      if self.__terminated:
        self.__terminate_event.set()
        return

  def start(self):
    self.__start_event.set()

  def terminate(self):
    self.__terminated = True
    self.__start_event.set()

  def terminate_and_join(self):
    self.terminate()
    if self.started:
#      print >>sys.stderr, 'waiting for terminate event'
      self.__terminate_event.wait()

free_lock = Lock()

def get_free_task(task_list):
  free_lock.acquire()
  for t in task_list:
    if not t.started:
      t.started = True
      free_lock.release()
      return t
  free_lock.release()
  return None

def map_task(source, func, thread_limit=10):
  '''Run func in up to thread_limit threads, with data from
  source arg passed into it.

  The arg source must be iterable. map_task() will call next()
  each time a free thread is available.

  The function will block until all of the tasks are completed.
  '''
  assert thread_limit > 0
  e = BoundedSemaphore(thread_limit)
  task_list = []
  for i in xrange(0, thread_limit):
    task_list.append(task(func, e))
  iterer = source.__iter__()
  data = None
  while 1:
    try:
      if data is None:
        data = iterer.next()
      t = get_free_task(task_list)
      if t:
        t.data = data
        t.start()
        data = None
      else:
#        print >> sys.stderr, 'waiting for e'
        e.acquire()
        e.release()
    except StopIteration:
    # iteration is stopped
#      print >>sys.stderr, 'terminating'
      for a_task in task_list:
#        print >>sys.stderr, 'terminating ' + str(a_task)
        a_task.terminate_and_join()
      return
