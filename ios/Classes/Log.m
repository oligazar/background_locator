//
//  Log.m
//  background_locator
//
//  Created by Alexander on 20/5/21.
//

#import "Log.h"

@implementation Log

void append(NSString *msg) {
    NSDate *now = [[NSDate alloc] init];
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"MM/dd'T'kk:mm:ss.SSSZ";
    NSString *dateStr = [formatter stringFromDate:now];
    formatter.dateFormat = @"yyMMdd";
    NSString *dayStr = [formatter stringFromDate:now];
    
    NSString *fileName = [NSString stringWithFormat:@"log_%@.txt", dayStr];
    NSString *text = [NSString stringWithFormat:@"%@ %@", dateStr, msg];
    
    NSFileManager *fm = [NSFileManager defaultManager];
    NSURL *documentURL = [fm URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask][0];
    NSURL *logURL = [documentURL URLByAppendingPathComponent:fileName];
    
    NSError *error;
    NSFileHandle *handle = [NSFileHandle fileHandleForWritingToURL:logURL error:&error];
    if (handle != nil) {
        [handle seekToEndOfFile];
        [handle writeData:[text dataUsingEncoding:NSUTF8StringEncoding]];
        [handle closeFile];
    } else {
        [text writeToURL:logURL atomically:YES encoding:NSUTF8StringEncoding error:&error];
    }
}

void _Log(const char *file, int lineNumber, const char *funcName, NSString *format,...) {
    va_list ap;
    va_start (ap, format);
    format = [format stringByAppendingString:@"\n"];
    NSString *msg = [[NSString alloc] initWithFormat:[NSString stringWithFormat:@"%@",format] arguments:ap];
    va_end (ap);
    
    NSString *text = [NSString stringWithFormat:@"%s:%3d - %s", funcName, lineNumber, [msg UTF8String]];
    NSLog(@"%@", text);
    append(text);
}

@end
