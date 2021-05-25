//
//  Log.h
//  background_locator
//
//  Created by Alexander on 20/5/21.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

#define BLLog(args...) _Log(__FILE__,__LINE__,__PRETTY_FUNCTION__,args);

@interface Log : NSObject

void _Log(const char *file, int lineNumber, const char *funcName, NSString *format,...);

@end

NS_ASSUME_NONNULL_END
